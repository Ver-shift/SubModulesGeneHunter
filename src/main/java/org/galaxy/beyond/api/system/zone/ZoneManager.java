package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.BeyondAttachmentInit;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.algorithm.NodeAwareExpander;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Zone 数据管理 —— 只负责 zone 的增删改查，不做 cap 管理和事件分发。
 * <p>
 * 活跃区域使用圆形扩张算法（欧几里得距离），视觉上更自然。
 */
public class ZoneManager implements IZoneManager {

    private final NodeAwareExpander nodeExpander = new NodeAwareExpander(this::groupComponents);

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

    // ---- Safe Zone ----

    @Override
    public void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center) {
        ChunkPos cc = ChunkPos.containing(center);
        int radius = chunkSize / 2;
        LevelZoneData lzd = getLZD(serverLevel);
        Set<ChunkPos> targets = ZoneHelper.expandSquare(cc, radius);

        // 扩容时检查节点是否全部解锁
        if (lzd.getZoneEntries().stream().anyMatch(e -> e.getValue() == ZoneType.Safe_Zone)) {
            var rogueData = BeyondAPI.getBeyondDimensionData(serverLevel).getRogueData();
            for (ChunkPos p : targets) {
                if (lzd.getZoneType(p) != ZoneType.Node_Zone) continue;
                var nd = rogueData.findNodeData(p);
                if (nd != null && nd.getPhase() != NodePhase.UNLOCKED) {
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                            net.minecraft.network.chat.Component.translatable("commands.beyond.safezone.expand.locked"), false);
                    return;
                }
            }
        }

        boolean changed = false;
        for (ChunkPos p : targets) {
            if (lzd.addZone(p, ZoneType.Safe_Zone)) changed = true;
        }
        if (changed) syncLevelData(serverLevel);
    }

    // ---- Node Zone ----

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
            var nd = new NodeData(randomNodeColor(serverLevel));
            for (ChunkPos cp : chunks) {
                if (cp != null) nd.addChunkPos(cp);
            }
            if (!nd.getNodeChunks().isEmpty()) {
                rogueData.addNodeData(nd);
            }
            syncLevelData(serverLevel);
        }
    }

    private static final NodeColor[] NODE_COLORS = { NodeColor.GREEN, NodeColor.ORANGE, NodeColor.RED };
    private static NodeColor randomNodeColor(ServerLevel level) {
        return NODE_COLORS[level.getRandom().nextInt(NODE_COLORS.length)];
    }

    // ---- Active Zone Init ----

    @Override
    public void activeZoneInit(ServerLevel serverLevel) {
        var entries = getLZD(serverLevel).getZoneEntries();
        var safe = ZoneHelper.filterByMask(entries, ZoneType.Safe_Zone.mask());
        if (safe.isEmpty()) return;

        Set<ChunkPos> seeds = new HashSet<>(safe.chunks());
        Set<ChunkPos> nodeChunks = getNodeChunks(entries);
        Set<ChunkPos> uncompleted = getUncompletedNodes(serverLevel, nodeChunks);

        int minRadius = CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get();

        int actualRadius = nodeExpander.expandUntilWrapped(serverLevel, seeds, uncompleted,
                Integer.MAX_VALUE, minRadius, maxRadius,
                (p, t) -> addZone(serverLevel, p, t),
                p -> getLZD(serverLevel).getZoneType(p));

        if (actualRadius >= minRadius) syncLevelData(serverLevel);
    }

    @Override
    public void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData) {
        List<ChunkPos> seedList = nodeData.getNodeData().getNodeChunkPosList();
        if (seedList.isEmpty()) return;

        Set<ChunkPos> seeds = new HashSet<>(seedList);
        var entries = getLZD(serverLevel).getZoneEntries();
        Set<ChunkPos> nodeChunks = getNodeChunks(entries);
        Set<ChunkPos> uncompleted = getUncompletedNodes(serverLevel, nodeChunks);

        int needed = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();
        int minRadius = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();

        int actualRadius = nodeExpander.expandUntilWrapped(serverLevel, seeds, uncompleted,
                needed, minRadius, maxRadius,
                (p, t) -> addZone(serverLevel, p, t),
                p -> getLZD(serverLevel).getZoneType(p));

        if (actualRadius >= minRadius) syncLevelData(serverLevel);
    }

    // ---- 节点组件分组 ----

    private Set<Set<ChunkPos>> groupComponents(Set<ChunkPos> uncompleted) {
        Set<Set<ChunkPos>> components = new HashSet<>();
        Set<ChunkPos> remaining = new HashSet<>(uncompleted);
        while (!remaining.isEmpty())
            components.add(extractComponent(remaining));
        return components;
    }

    private static Set<ChunkPos> extractComponent(Set<ChunkPos> remaining) {
        Set<ChunkPos> comp = new HashSet<>();
        ChunkPos seed = remaining.iterator().next();
        remaining.remove(seed);
        comp.add(seed);
        Deque<ChunkPos> queue = new ArrayDeque<>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            for (ChunkPos nb : neighbors8(queue.poll())) {
                if (remaining.remove(nb)) {
                    comp.add(nb);
                    queue.add(nb);
                }
            }
        }
        return comp;
    }

    // ---- 辅助 ----

    private static Set<ChunkPos> getNodeChunks(Set<Map.Entry<ChunkPos, ZoneType>> entries) {
        return entries.stream()
                .filter(e -> e.getValue().matches(ZoneType.Node_Zone.mask()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private static Set<ChunkPos> getUncompletedNodes(ServerLevel level, Set<ChunkPos> nodeChunks) {
        var rogueData = BeyondAPI.getBeyondDimensionData(level).getRogueData();
        Set<ChunkPos> uncompleted = new HashSet<>(nodeChunks);
        for (NodeData nd : rogueData.getNodeDatas()) {
            if (nd.getPhase() == NodePhase.UNLOCKED) {
                for (Long packed : nd.getNodeChunks()) {
                    uncompleted.remove(new ChunkPos(ChunkPos.getX(packed), ChunkPos.getZ(packed)));
                }
            }
        }
        return uncompleted;
    }

    private static List<ChunkPos> neighbors8(ChunkPos p) {
        int cx = p.getMinBlockX() >> 4, cz = p.getMinBlockZ() >> 4;
        return List.of(
                new ChunkPos(cx + 1, cz), new ChunkPos(cx - 1, cz),
                new ChunkPos(cx, cz + 1), new ChunkPos(cx, cz - 1),
                new ChunkPos(cx + 1, cz + 1), new ChunkPos(cx - 1, cz - 1),
                new ChunkPos(cx + 1, cz - 1), new ChunkPos(cx - 1, cz + 1));
    }
}
