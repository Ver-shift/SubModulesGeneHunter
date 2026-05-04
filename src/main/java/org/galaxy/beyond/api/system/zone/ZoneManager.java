package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureData;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;
import java.util.stream.Collectors;

public class ZoneManager implements IZoneManager {


    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel serverLevel)) return;

        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos worldPos = new BlockPos(chunk.getPos().x() << 4, 0, chunk.getPos().z() << 4);
        if (structureManager.getStructureBoundingBox(serverLevel, worldPos) != null) {
            addNodeZone(serverLevel, worldPos);
        }
    }

    private LevelZoneData getLZD(ServerLevel level) {
        return BeyondAPI.getBeyondLevelData(level).getLevelZoneData();
    }

    @Override
    public void addZone(ServerLevel serverLevel, ChunkPos pos, ZoneType zoneType) {
        getLZD(serverLevel).addZone(pos, zoneType);
    }

    @Override
    public void safeZoneInit(ServerLevel serverLevel) {
        SafeZoneStructureData data = BeyondAPI.getBeyondLevelData(serverLevel).getSafeZoneStructureData();
        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();

        // 1. 在世界出生点附近查找村庄结构
        BlockPos searchPos = serverLevel.getRespawnData().pos();
        BlockPos nearest = serverLevel.findNearestMapStructure(StructureTags.VILLAGE, searchPos, 500, false);
        if (nearest == null) return;

        // 2. 获取村庄边界框与区块列表
        BoundingBox box = structureManager.getStructureBoundingBox(serverLevel, nearest);
        if (box == null) return;

        // 3. 在结构覆盖的区块中搜索安全出生点（优先从中心区块开始）
        List<ChunkPos> chunks = structureManager.getStructureChunks(serverLevel, nearest);
        BlockPos safePos = findSafePositionInChunks(serverLevel, chunks, box);
        if (safePos != null) {
            data.setSpawnPos(safePos);
            data.setInitialized(1);
        }

        // 4. 注册安全区
        BlockPos center = box.getCenter();
        data.setCenterPos(center);
        int xChunks = (box.getXSpan() + 15) / 16;
        int zChunks = (box.getZSpan() + 15) / 16;
        int chunkSize = Math.max(xChunks, zChunks);
        if (chunkSize % 2 == 0) chunkSize++;
        addSafeZone(serverLevel, chunkSize, center);
    }

    /**
     * 在结构覆盖的区块中搜索安全的站立位置，优先选择靠近边界框中心、能看到天空的位置。
     */
    private BlockPos findSafePositionInChunks(ServerLevel level, List<ChunkPos> chunks, BoundingBox box) {
        BlockPos center = box.getCenter();

        // 按距离中心区块排序，优先搜索中心区域
        List<ChunkPos> sortedChunks = chunks.stream()
                .sorted((a, b) -> {
                    int da = Math.abs(a.x() - (center.getX() >> 4)) + Math.abs(a.z() - (center.getZ() >> 4));
                    int db = Math.abs(b.x() - (center.getX() >> 4)) + Math.abs(b.z() - (center.getZ() >> 4));
                    return Integer.compare(da, db);
                })
                .toList();

        for (ChunkPos chunkPos : sortedChunks) {
            // 在每个区块的中心附近搜索
            int startX = (chunkPos.x() << 4) + 4;
            int startZ = (chunkPos.z() << 4) + 4;
            for (int dx = 0; dx < 8; dx++) {
                for (int dz = 0; dz < 8; dz++) {
                    int x = startX + dx;
                    int z = startZ + dz;
                    if (x < box.minX() || x > box.maxX() || z < box.minZ() || z > box.maxZ()) continue;

                    BlockPos ground = findGround(level, x, z);
                    if (ground != null && isSafeStandingPosition(level, ground)) {
                        return ground;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查找指定 (x, z) 处的地面高度（上方第一个空气位置）。
     */
    private BlockPos findGround(ServerLevel level, int x, int z) {
        int maxY = level.getMinY() + level.getHeight() - 1;
        for (int y = maxY; y >= level.getMinY(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (isValidGround(state)) {
                return pos.above();
            }
        }
        return null;
    }

    /**
     * 检查位置是否可以安全站立。
     */
    public static boolean isSafeStandingPosition(ServerLevel level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        boolean groundSolid = isValidGround(belowState);
        boolean hasSpace = feetState.isAir() && (headState.isAir() || !headState.blocksMotion());
        boolean openSky = level.canSeeSky(pos);

        return groundSolid && hasSpace && openSky;
    }

    /**
     * 脚下必须是固体方块（排除空气、液体、树叶、火等）。
     */
    public static boolean isValidGround(BlockState state) {
        return !state.isAir()
                && !state.is(Blocks.WATER)
                && !state.is(Blocks.LAVA)
                && !state.is(Blocks.FIRE)
                && !state.is(Blocks.CAMPFIRE)
                && !state.is(Blocks.SOUL_CAMPFIRE)
                && state.blocksMotion();
    }



    @Override
    public void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center) {
        ChunkPos centerChunk = ChunkPos.containing(center);
        int radius = chunkSize / 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                addZone(serverLevel, new ChunkPos(centerChunk.x() + dx, centerChunk.z() + dz), ZoneType.Safe_Zone);
            }
        }
    }

    @Override
    public void addNodeZone(ServerLevel serverLevel, BlockPos pos) {
        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = structureManager.getStructureChunks(serverLevel, pos);
        for (ChunkPos chunkPos : chunks) {
            addZone(serverLevel, chunkPos, ZoneType.Node_Zone);
        }
    }

    @Override
    public void activeZoneInit(ServerLevel serverLevel) {
        LevelZoneData lzd = getLZD(serverLevel);
        Map<ChunkPos, ZoneType> zones = lzd.getLevelZone();
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
                fillActiveZone(serverLevel, zones, exMinX, exMinZ, exMaxX, exMaxZ);
                return;
            }
            expand++;
        }
    }

    @Override
    public void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData) {
        LevelZoneData lzd = getLZD(serverLevel);
        Map<ChunkPos, ZoneType> zones = lzd.getLevelZone();

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
                fillActiveZone(serverLevel, zones, exMinX, exMinZ, exMaxX, exMaxZ);
                return;
            }
            r++;
        }
    }

    /**
     * 从 zone 映射中提取指定类型的所有区块。
     */
    private Set<ChunkPos> getChunksByType(Map<ChunkPos, ZoneType> zones, ZoneType type) {
        return zones.entrySet().stream()
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
     */
    private void fillActiveZone(ServerLevel serverLevel, Map<ChunkPos, ZoneType> zones,
                                int minX, int minZ, int maxX, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                ChunkPos pos = new ChunkPos(x, z);
                if (!zones.containsKey(pos)) {
                    addZone(serverLevel, pos, ZoneType.Active_Zone);
                }
            }
        }
    }

    @Override
    public void addCap(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getOrCreateZoneData(type);
        zoneData.addCap(zoneCapType);
    }

    @Override
    public void removeCap(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getZoneData(type);
        if (zoneData != null) {
            zoneData.removeCap(zoneCapType);
        }
    }

    @Override
    public void clearCap(ServerLevel serverLevel, ZoneType type) {
        LevelZoneData lzd = getLZD(serverLevel);
        ZoneData zoneData = lzd.getZoneData(type);
        if (zoneData != null) {
            zoneData.clearCaps();
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
        }
    }
}
