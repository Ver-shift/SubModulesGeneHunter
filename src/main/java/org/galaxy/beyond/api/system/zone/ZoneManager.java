package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.event.custom.PlayerChangeZoneEvent;
import org.galaxy.beyond.api.init.BeyondAttachmentInit;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.BeyondPlayerData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ZoneManager implements IZoneManager {


    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel serverLevel)) return;

        // 安全区未初始化时，不生成节点区域（初始化前的节点被安全区吸收）
        var dimData = BeyondAPI.getBeyondDimensionData(serverLevel);
        if (dimData.getSafeZoneStructureData().getInitialized() < 1) return;

        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos worldPos = new BlockPos(chunk.getPos().x() << 4, 0, chunk.getPos().z() << 4);
        // TODO: 后续改为配置文件指定的结构标签，目前使用所有mod结构
        if (structureManager.hasAnyStructure(serverLevel, worldPos)) {
            addNodeZone(serverLevel, worldPos);
        }
    }

    private LevelZoneData getLZD(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(level).getLevelZoneData();
    }

    private void syncLevelData(ServerLevel level) {
        level.syncData(BeyondAttachmentInit.GLOBAL_DATA.get());
    }

    @Override
    public boolean addZone(ServerLevel serverLevel, ChunkPos pos, ZoneType zoneType) {
        return getLZD(serverLevel).addZone(pos, zoneType);
    }

    @Override
    public void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center) {
        ChunkPos centerChunk = ChunkPos.containing(center);
        int radius = chunkSize / 2;
        LevelZoneData lzd = getLZD(serverLevel);
        boolean changed = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos pos = new ChunkPos(centerChunk.x() + dx, centerChunk.z() + dz);
                if (addZone(serverLevel, pos, ZoneType.Safe_Zone)) {
                    changed = true;
                }
            }
        }
        if (changed) syncLevelData(serverLevel);
    }

    @Override
    public void addNodeZone(ServerLevel serverLevel, BlockPos pos) {
        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = structureManager.getStructureChunks(serverLevel, pos);
        LevelZoneData lzd = getLZD(serverLevel);
        boolean changed = false;
        for (ChunkPos chunkPos : chunks) {
            if (addZone(serverLevel, chunkPos, ZoneType.Node_Zone)) {
                changed = true;
            }
        }
        if (changed) syncLevelData(serverLevel);
    }

    @Override
    public void activeZoneInit(ServerLevel serverLevel) {
        LevelZoneData lzd = getLZD(serverLevel);
        Set<Map.Entry<ChunkPos, ZoneType>> zones = lzd.getZoneEntries();
        Set<ChunkPos> safeChunks = getChunksByType(zones, ZoneType.Safe_Zone);
        if (safeChunks.isEmpty()) return;

        Set<ChunkPos> allNodeChunks = getChunksByType(zones, ZoneType.Node_Zone);

        // 安全区包围盒
        int safeMinX = safeChunks.stream().mapToInt(ChunkPos::x).min().orElse(0);
        int safeMaxX = safeChunks.stream().mapToInt(ChunkPos::x).max().orElse(0);
        int safeMinZ = safeChunks.stream().mapToInt(ChunkPos::z).min().orElse(0);
        int safeMaxZ = safeChunks.stream().mapToInt(ChunkPos::z).max().orElse(0);

        int minExpand = CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
        int minNodes = CommonConfig.ACTIVE_ZONE_MIN_NODES.get();
        int expand = minExpand;
        int maxIterations = 200;

        while (expand < maxIterations) {
            int exMinX = safeMinX - expand;
            int exMaxX = safeMaxX + expand;
            int exMinZ = safeMinZ - expand;
            int exMaxZ = safeMaxZ + expand;

            int nodesCovered = countChunkClusters(allNodeChunks, exMinX, exMinZ, exMaxX, exMaxZ);
            if (nodesCovered >= minNodes && expand >= minExpand) {
                if (fillActiveZone(serverLevel, zones, exMinX, exMinZ, exMaxX, exMaxZ)) {
                    syncLevelData(serverLevel);
                }
                return;
            }
            expand++;
        }
    }

    @Override
    public void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData) {
        LevelZoneData lzd = getLZD(serverLevel);
        Set<Map.Entry<ChunkPos, ZoneType>> zones = lzd.getZoneEntries();

        List<ChunkPos> nodeChunks = nodeData.getNodeData().getNodeChunks();
        if (nodeChunks.isEmpty()) return;

        int nodeMinX = nodeChunks.stream().mapToInt(ChunkPos::x).min().orElse(0);
        int nodeMaxX = nodeChunks.stream().mapToInt(ChunkPos::x).max().orElse(0);
        int nodeMinZ = nodeChunks.stream().mapToInt(ChunkPos::z).min().orElse(0);
        int nodeMaxZ = nodeChunks.stream().mapToInt(ChunkPos::z).max().orElse(0);

        Set<ChunkPos> allNodeChunks = getChunksByType(zones, ZoneType.Node_Zone);
        Set<ChunkPos> selfChunks = new HashSet<>(nodeChunks);
        int radius = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int minConnections = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();

        int r = radius;
        while (r < 200) {
            int exMinX = nodeMinX - r;
            int exMaxX = nodeMaxX + r;
            int exMinZ = nodeMinZ - r;
            int exMaxZ = nodeMaxZ + r;

            Set<ChunkPos> othersInRange = allNodeChunks.stream()
                    .filter(p -> p.x() >= exMinX && p.x() <= exMaxX && p.z() >= exMinZ && p.z() <= exMaxZ)
                    .collect(Collectors.toCollection(HashSet::new));
            othersInRange.removeAll(selfChunks);

            int connections = countChunkClusters(othersInRange, exMinX, exMinZ, exMaxX, exMaxZ);
            if (connections >= minConnections) {
                if (fillActiveZone(serverLevel, zones, exMinX, exMinZ, exMaxX, exMaxZ)) {
                    syncLevelData(serverLevel);
                }
                return;
            }
            r++;
        }
    }

    /**
     * 从 zone 映射中提取指定类型的所有区块。
     */
    private Set<ChunkPos> getChunksByType(Set<Map.Entry<ChunkPos, ZoneType>> zones, ZoneType type) {
        return zones.stream()
                .filter(e -> e.getValue() == type)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * 统计矩形区域内连通分量的个数，每个分量对应一个独立的节点。
     */
    private int countChunkClusters(Set<ChunkPos> chunkSet, int minX, int minZ, int maxX, int maxZ) {
        Set<ChunkPos> inArea = chunkSet.stream()
                .filter(p -> p.x() >= minX && p.x() <= maxX && p.z() >= minZ && p.z() <= maxZ)
                .collect(Collectors.toCollection(HashSet::new));

        int clusters = 0;
        while (!inArea.isEmpty()) {
            ChunkPos seed = inArea.iterator().next();
            floodFill(inArea, seed);
            clusters++;
        }
        return clusters;
    }

    /**
     * 四邻域洪泛填充，从集合中移除与 seed 连通的所有区块。
     */
    private void floodFill(Set<ChunkPos> remaining, ChunkPos seed) {
        Deque<ChunkPos> stack = new ArrayDeque<>();
        stack.push(seed);
        while (!stack.isEmpty()) {
            ChunkPos p = stack.pop();
            if (!remaining.remove(p)) continue;
            stack.push(new ChunkPos(p.x() + 1, p.z()));
            stack.push(new ChunkPos(p.x() - 1, p.z()));
            stack.push(new ChunkPos(p.x(), p.z() + 1));
            stack.push(new ChunkPos(p.x(), p.z() - 1));
        }
    }

    /**
     * 将矩形区域内尚未归属任何 zone 的区块注册为 Active_Zone。
     * @return 是否有新区块被添加
     */
    private boolean fillActiveZone(ServerLevel serverLevel, Set<Map.Entry<ChunkPos, ZoneType>> zones,
                                int minX, int minZ, int maxX, int maxZ) {
        Set<ChunkPos> existing = zones.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        boolean changed = false;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                ChunkPos pos = new ChunkPos(x, z);
                if (!existing.contains(pos)) {
                    if (addZone(serverLevel, pos, ZoneType.Active_Zone)) {
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public void addCap(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getOrCreateZoneData(type);
        zoneData.addCap(zoneCapType);
        syncLevelData(serverLevel);
    }

    @Override
    public void removeCap(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getZoneData(type);
        if (zoneData != null) {
            zoneData.removeCap(zoneCapType);
            syncLevelData(serverLevel);
        }
    }

    @Override
    public void clearCap(ServerLevel serverLevel, ZoneType type) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getZoneData(type);
        if (zoneData != null) {
            zoneData.clearCaps();
            syncLevelData(serverLevel);
        }
    }

    @Override
    public List<ZoneCapType> getCaps(ServerLevel serverLevel, ZoneType type) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getZoneData(type);
        if (zoneData == null) return List.of();
        return zoneData.getZoneCaps().stream()
                .map(ZoneCapData::getType)
                .toList();
    }

    @Override
    public void setCapLevel(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType, int capLevel) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getZoneData(type);
        if (zoneData == null) return;
        ZoneCapData capData = zoneData.getCapData(zoneCapType);
        if (capData != null) {
            capData.setLevel(capLevel);
            syncLevelData(serverLevel);
        }
    }

    @Override
    public void addCapLevel(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType, int capLevel) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getZoneData(type);
        if (zoneData == null) return;
        ZoneCapData capData = zoneData.getCapData(zoneCapType);
        if (capData != null) {
            capData.addLevel(capLevel);
            syncLevelData(serverLevel);
        }
    }

    @Override
    public void handleZoneRule(ServerLevel level) {
        LevelZoneData lzd = getLZD(level);
        Set<ZoneType> tickedZones = new HashSet<>();

        for (ServerPlayer player : level.players()) {
            BeyondPlayerData playerData = BeyondAPI.getBeyondPlayerData(player);

            ZoneType oldZone = playerData.getPlayerZoneData().getCurrentZone();
            ZoneData newZoneData = lzd.getZoneData(player.getOnPos());
            ZoneType newZone = newZoneData != null ? newZoneData.getZone() : ZoneType.Empty;
            ZoneData oldZoneData = lzd.getZoneData(oldZone);

            if (tickedZones.add(newZone)) {
                dispatch(newZoneData, cap -> cap.levelTick(level, newZone));
            }

            dispatch(newZoneData, cap -> cap.playerTick(player, newZone));

            if (oldZone != newZone) {
                playerData.getPlayerZoneData().setCurrentZone(newZone);
                NeoForge.EVENT_BUS.post(new PlayerChangeZoneEvent(player, oldZone, newZone));

                if (oldZone != ZoneType.Empty) {
                    dispatch(oldZoneData, cap -> cap.playerChangeZone(player, oldZone, newZone));
                    dispatch(newZoneData, cap -> cap.playerChangeZone(player, oldZone, newZone));
                }
            }
        }
    }

    @Override
    public void handlePlayerRightClickBlock(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) return;
        ServerLevel level = player.level();
        ZoneData zoneData = getLZD(level).getZoneData(pos);
        Block block = level.getBlockState(pos).getBlock();
        dispatch(zoneData, cap -> cap.playerRightClickBlock(player, block));
    }

    @Override
    public void handleMobTick(Mob mob) {
        if (mob == null) return;
        ServerLevel level = (ServerLevel) mob.level();
        ZoneData zoneData = getLZD(level).getZoneData(mob.blockPosition());
        if (zoneData == null) return;
        dispatch(zoneData, cap -> cap.mobTick(mob, zoneData.getZone()));
    }

    private static void dispatch(ZoneData zoneData, Consumer<ZoneCapType> action) {
        if (zoneData == null) return;
        for (ZoneCapData capData : zoneData.getZoneCaps()) {
            action.accept(capData.getType());
        }
    }
}
