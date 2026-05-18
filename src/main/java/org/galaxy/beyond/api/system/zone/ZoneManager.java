package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.BeyondPlayerData;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.event.custom.PlayerChangeZoneEvent;
import org.galaxy.beyond.api.init.BeyondAttachmentInit;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ZoneManager implements IZoneManager {

    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel serverLevel)) return;

        var dimData = BeyondAPI.getBeyondDimensionData(serverLevel);
        if (dimData.getSafeZoneStructureData().getInitialized() < 1) return;

        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos worldPos = new BlockPos(chunk.getPos().x() << 4, 0, chunk.getPos().z() << 4);
        if (structureManager.hasAnyStructure(serverLevel, worldPos)) {
            addNodeZone(serverLevel, worldPos);
        }
    }

    // ---- 内部辅助 ----

    private LevelZoneData getLZD(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(level).getLevelZoneData();
    }

    private void syncLevelData(ServerLevel level) {
        level.syncData(BeyondAttachmentInit.GLOBAL_DATA.get());
    }

    // ---- 基础 Zone 操作 ----

    @Override
    public boolean addZone(ServerLevel serverLevel, ChunkPos pos, ZoneType zoneType) {
        return getLZD(serverLevel).addZone(pos, zoneType);
    }

    @Override
    public void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center) {
        ChunkPos cc = ChunkPos.containing(center);
        int radius = chunkSize / 2;
        LevelZoneData lzd = getLZD(serverLevel);
        boolean changed = false;
        for (ChunkPos p : ZoneHelper.expandSquare(cc, radius)) {
            if (lzd.addZone(p, ZoneType.Safe_Zone)) changed = true;
        }
        if (changed) syncLevelData(serverLevel);
    }

    @Override
    public void addNodeZone(ServerLevel serverLevel, BlockPos pos) {
        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = structureManager.getStructureChunks(serverLevel, pos);
        LevelZoneData lzd = getLZD(serverLevel);
        boolean changed = false;
        for (ChunkPos cp : chunks) {
            if (cp == null) continue;
            ZoneType existing = lzd.getZoneType(cp);
            if (existing != null && existing.matches(ZoneType.Safe_Zone.mask())) continue;
            if (lzd.addZone(cp, ZoneType.Node_Zone)) changed = true;
        }
        if (changed) {
            var rogueData = BeyondAPI.getBeyondDimensionData(serverLevel).getRogueData();
            ChunkPos first = chunks.getFirst();
            rogueData.getNodeDataMap().putIfAbsent(first,
                    new org.galaxy.beyond.api.system.node.NodeData(randomNodeColor(serverLevel)));
            syncLevelData(serverLevel);
        }
    }

    private static final org.galaxy.beyond.api.system.node.NodeColor[] NODE_COLORS = {
            org.galaxy.beyond.api.system.node.NodeColor.GREEN,
            org.galaxy.beyond.api.system.node.NodeColor.ORANGE,
            org.galaxy.beyond.api.system.node.NodeColor.RED
    };
    private static org.galaxy.beyond.api.system.node.NodeColor randomNodeColor(ServerLevel level) {
        return NODE_COLORS[level.getRandom().nextInt(NODE_COLORS.length)];
    }

    // ---- Active Zone 初始化与扩展 ----

    @Override
    public void activeZoneInit(ServerLevel serverLevel) {
        LevelZoneData lzd = getLZD(serverLevel);
        Set<Map.Entry<ChunkPos, ZoneType>> entries = lzd.getZoneEntries();

        var safe = ZoneHelper.filterByMask(entries, ZoneType.Safe_Zone.mask());
        if (safe.isEmpty()) return;
        var nodes = ZoneHelper.filterByMask(entries, ZoneType.Node_Zone.mask());
        ZoneHelper.Bounds safeBounds = safe.bounds();

        var cfg = BeyondAPI.getGlobalData(serverLevel).getRogueConfig();
        int minExpand = cfg.getMinNodeExpandChunks();
        int maxExpand = cfg.getMaxNodeExpandRange();
        int minNodes  = cfg.getMinNodeExpandCount();

        // 从安全区向外逐层扩张，直到找到足够节点或达到最大距离
        ZoneHelper.Bounds best = null;
        for (int expand = minExpand; expand <= maxExpand; expand++) {
            ZoneHelper.Bounds b = safeBounds.expanded(expand);
            if (countClusters(nodes.chunks(), b) >= minNodes) {
                if (fillActiveZone(serverLevel, entries, b)) syncLevelData(serverLevel);
                return;
            }
            best = b;
        }
        // 达到最大距离仍未满足节点数，以最大范围注册（best 即 maxExpand 对应范围）
        if (best != null && fillActiveZone(serverLevel, entries, best)) {
            syncLevelData(serverLevel);
        }
    }

    @Override
    public void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData) {
        LevelZoneData lzd = getLZD(serverLevel);
        Set<Map.Entry<ChunkPos, ZoneType>> entries = lzd.getZoneEntries();
        List<ChunkPos> selfList = nodeData.getNodeData().getNodeChunks();
        if (selfList.isEmpty()) return;

        var allNodes = entries.stream()
                .filter(e -> e.getValue().matches(ZoneType.Node_Zone.mask()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        var completedNodes = BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld())
                .getRogueData()
                .getCompletedNodeChunks();
        var plan = ActiveZoneExpansionPlanner.plan(
                toPlannerChunks(selfList),
                toPlannerChunks(allNodes),
                toPlannerChunks(completedNodes),
                CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get(),
                CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get(),
                CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get());
        ZoneHelper.Bounds bounds = new ZoneHelper.Bounds(
                plan.bounds().minX(),
                plan.bounds().minZ(),
                plan.bounds().maxX(),
                plan.bounds().maxZ());

        if (fillActiveZone(serverLevel, entries, bounds)) {
            syncLevelData(serverLevel);
        }
    }

    private Set<ActiveZoneExpansionPlanner.Chunk> toPlannerChunks(Collection<ChunkPos> chunks) {
        return chunks.stream()
                .map(chunk -> new ActiveZoneExpansionPlanner.Chunk(chunk.x(), chunk.z()))
                .collect(Collectors.toSet());
    }

    /** 将矩形区域内尚未归属任何 zone 的区块注册为 Active_Zone */
    private boolean fillActiveZone(ServerLevel serverLevel,
                                   Set<Map.Entry<ChunkPos, ZoneType>> entries, ZoneHelper.Bounds b) {
        Set<ChunkPos> existing = entries.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
        boolean changed = false;
        for (ChunkPos p : b.allChunks()) {
            if (!existing.contains(p) && addZone(serverLevel, p, ZoneType.Active_Zone)) {
                changed = true;
            }
        }
        return changed;
    }

    // ---- 连通分量统计 ----

    private static int countClusters(Set<ChunkPos> chunks, ZoneHelper.Bounds b) {
        Set<ChunkPos> inArea = chunks.stream()
                .filter(b::contains).collect(Collectors.toCollection(HashSet::new));
        int clusters = 0;
        while (!inArea.isEmpty()) {
            floodFill(inArea, inArea.iterator().next());
            clusters++;
        }
        return clusters;
    }

    private static void floodFill(Set<ChunkPos> remaining, ChunkPos seed) {
        Deque<ChunkPos> stack = new ArrayDeque<>();
        stack.push(seed);
        while (!stack.isEmpty()) {
            ChunkPos p = stack.pop();
            if (!remaining.remove(p)) continue;
            for (ChunkPos n : ZoneHelper.neighbors4(p)) stack.push(n);
        }
    }

    // ---- Cap 操作 ----

    @Override
    public void addCap(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType) {
        ZoneData zd = getLZD(serverLevel).getOrCreateZoneData(type);
        zd.addCap(zoneCapType);
        syncLevelData(serverLevel);
    }

    @Override
    public void removeCap(ServerLevel serverLevel, ZoneType type, ZoneCapType zoneCapType) {
        ZoneData zd = getLZD(serverLevel).getZoneData(type);
        if (zd != null) { zd.removeCap(zoneCapType); syncLevelData(serverLevel); }
    }

    @Override
    public void clearCap(ServerLevel serverLevel, ZoneType type) {
        ZoneData zd = getLZD(serverLevel).getZoneData(type);
        if (zd != null) { zd.clearCaps(); syncLevelData(serverLevel); }
    }

    @Override
    public List<ZoneCapType> getCaps(ServerLevel serverLevel, ZoneType type) {
        ZoneData zd = getLZD(serverLevel).getZoneData(type);
        if (zd == null) return List.of();
        return zd.getZoneCaps().stream().map(ZoneCapData::getType).toList();
    }

    @Override
    public void setCapLevel(ServerLevel serverLevel, ZoneType type, ZoneCapType cap, int level) {
        ZoneData zd = getLZD(serverLevel).getZoneData(type);
        if (zd == null) return;
        ZoneCapData cd = zd.getCapData(cap);
        if (cd != null) { cd.setLevel(level); syncLevelData(serverLevel); }
    }

    @Override
    public void addCapLevel(ServerLevel serverLevel, ZoneType type, ZoneCapType cap, int delta) {
        ZoneData zd = getLZD(serverLevel).getZoneData(type);
        if (zd == null) return;
        ZoneCapData cd = zd.getCapData(cap);
        if (cd != null) { cd.addLevel(delta); syncLevelData(serverLevel); }
    }

    // ---- Tick / 事件处理 ----

    @Override
    public void handleZoneRule(ServerLevel level) {
        LevelZoneData lzd = getLZD(level);
        Set<ZoneType> tickedZones = EnumSet.noneOf(ZoneType.class);

        for (ServerPlayer player : level.players()) {
            BeyondPlayerData pd = BeyondAPI.getBeyondPlayerData(player);
            ZoneType oldZone = pd.getPlayerZoneData().getCurrentZone();
            ZoneData newZd = lzd.getZoneData(player.getOnPos());
            ZoneType newZone = newZd != null ? newZd.getZone() : ZoneType.Empty;
            ZoneData oldZd = lzd.getZoneData(oldZone);

            if (tickedZones.add(newZone)) {
                dispatch(newZd, cap -> cap.levelTick(level, newZone));
            }
            dispatch(newZd, cap -> cap.playerTick(player, newZone));

            if (oldZone != newZone) {
                pd.getPlayerZoneData().setCurrentZone(newZone);
                NeoForge.EVENT_BUS.post(new PlayerChangeZoneEvent(player, oldZone, newZone));
                dispatch(oldZd, cap -> cap.playerChangeZone(player, oldZone, newZone));
                dispatch(newZd, cap -> cap.playerChangeZone(player, oldZone, newZone));
            }
        }
    }

    @Override
    public void handlePlayerRightClickBlock(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) return;
        ServerLevel level = player.level();
        dispatch(getLZD(level).getZoneData(pos), cap -> cap.playerRightClickBlock(player, level.getBlockState(pos).getBlock()));
    }

    @Override
    public void handlePlayerUseItem(ServerPlayer player, Item item) {
        if (player == null) return;
        ServerLevel level = player.level();
        LevelZoneData lzd = getLZD(level);

        // 尝试按位置查找，若区块未注册则回退到 ZoneType 级查找/创建
        ZoneData zd = lzd.getZoneData(player.getOnPos());
        if (zd == null) zd = lzd.getZoneData(ZoneType.Active_Zone);
        if (zd == null) zd = lzd.getOrCreateZoneData(ZoneType.Active_Zone);

        dispatch(zd, cap -> cap.playerUseItem(player, item));
    }

    @Override
    public void handleMobTick(Mob mob) {
        if (mob == null || mob.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) mob.level();
        ZoneData zd = getLZD(level).getZoneData(mob.blockPosition());
        if (zd != null) dispatch(zd, cap -> cap.mobTick(mob, zd.getZone()));
    }

    private static void dispatch(ZoneData zd, Consumer<ZoneCapType> action) {
        if (zd == null) return;
        for (ZoneCapData cd : zd.getZoneCaps()) action.accept(cd.getType());
    }
}
