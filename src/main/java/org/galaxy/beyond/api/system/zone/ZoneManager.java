package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.algorithm.NodeAwareExpander;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;
import java.util.stream.Collectors;

public class ZoneManager implements IZoneManager {

    private final NodeAwareExpander nodeExpander = new NodeAwareExpander(this::groupComponents);

    private static final TagKey<Structure> NODE_STRUCTURE_TAG =
            TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("beyond", "node_structure"));

    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel sl)) return;
        if (BeyondAPI.getSafeZoneStructureData(sl).getInitialized() < 1) return;
        var sm = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos wp = new BlockPos(chunk.getPos().x() << 4, 0, chunk.getPos().z() << 4);
        if (sm.hasStructureByTag(sl, wp, NODE_STRUCTURE_TAG)) addNodeZone(sl, wp);
    }

    private LevelZoneData getLZD(ServerLevel l) { return BeyondAPI.getLevelZoneData(l); }
    private void syncLD(ServerLevel l) { BeyondAPI.syncGlobalData(l); }

    @Override
    public boolean addZone(ServerLevel level, ChunkPos pos, ZoneType type) {
        boolean changed = getLZD(level).addZone(pos, type);
        if (changed) syncLD(level);
        return changed;
    }

    // ---- Safe Zone ----

    @Override
    public void addSafeZone(ServerLevel level, int chunkSize, BlockPos center) {
        ChunkPos cc = ChunkPos.containing(center);
        int radius = chunkSize / 2;
        LevelZoneData lzd = getLZD(level);
        Set<ChunkPos> targets = ZoneHelper.expandSquare(cc, radius);

        if (!lzd.safeChunks().isEmpty()) {
            var rogueData = BeyondAPI.getRogueData(level);
            for (ChunkPos p : targets) {
                if (!lzd.isNode(p)) continue;
                var nd = rogueData.findNodeData(p);
                if (nd != null && nd.getPhase() != NodePhase.UNLOCKED) {
                    level.getServer().getPlayerList().broadcastSystemMessage(
                            net.minecraft.network.chat.Component.translatable("commands.beyond.safezone.expand.locked"), false);
                    return;
                }
            }
        }

        boolean changed = false;
        for (ChunkPos p : targets)
            if (lzd.addSafe(p)) changed = true;
        if (changed) syncLD(level);
    }

    // ---- Node Zone ----

    @Override
    public void addNodeZone(ServerLevel level, BlockPos pos) {
        var sm = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = sm.getStructureChunks(level, pos);
        LevelZoneData lzd = getLZD(level);
        boolean changed = false;
        for (ChunkPos cp : chunks) {
            if (cp == null) continue;
            if (lzd.isSafe(cp)) continue;
            if (lzd.addNode(cp)) changed = true;
        }
        if (changed) {
            var rogueData = BeyondAPI.getRogueData(level);
            var nd = new NodeData(randomNodeColor(level));
            for (ChunkPos cp : chunks) if (cp != null) nd.addChunkPos(cp);
            if (!nd.getNodeChunks().isEmpty()) rogueData.addNodeData(nd);
            syncLD(level);
        }
    }

    private static NodeColor randomNodeColor(ServerLevel l) {
        int g = CommonConfig.NODE_COLOR_GREEN_WEIGHT.get();
        int o = CommonConfig.NODE_COLOR_ORANGE_WEIGHT.get();
        int r = CommonConfig.NODE_COLOR_RED_WEIGHT.get();
        int total = g + o + r;
        if (total <= 0) return NodeColor.ORANGE;
        int roll = l.getRandom().nextInt(total);
        if (roll < g) return NodeColor.GREEN;
        if (roll < g + o) return NodeColor.ORANGE;
        return NodeColor.RED;
    }

    // ---- Active Zone ----

    @Override
    public void activeZoneInit(ServerLevel level) {
        LevelZoneData lzd = getLZD(level);
        Set<ChunkPos> safeChunks = lzd.safeChunks();
        if (safeChunks.isEmpty()) return;

        Set<ChunkPos> nodeChunks = lzd.nodeChunks();
        Set<ChunkPos> uncompleted = getUncompletedNodes(level, nodeChunks);

        int minR = CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
        int maxR = CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get();

        int r = nodeExpander.expandUntilWrapped(level, safeChunks, uncompleted,
                CommonConfig.ACTIVE_ZONE_MIN_NODES.get(), minR, maxR, lzd);
        if (r >= minR) syncLD(level);
    }

    @Override
    public void addActiveZone(ServerLevel level, RogueNodeData nodeData) {
        List<ChunkPos> list = nodeData.getNodeData().getNodeChunkPosList();
        if (list.isEmpty()) return;

        Set<ChunkPos> seeds = new HashSet<>(list);
        LevelZoneData lzd = getLZD(level);

        // 主动扫描附近的结构点位，预注册节点区域（无需等待区块加载）
        discoverNearbyStructures(level, seeds);

        Set<ChunkPos> nodeChunks = lzd.nodeChunks();
        Set<ChunkPos> uncompleted = getUncompletedNodes(level, nodeChunks);

        int need = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();
        int minR = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int maxR = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();

        int r = nodeExpander.expandUntilWrapped(level, seeds, uncompleted, need, minR, maxR, lzd);
        if (r < 0) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable("commands.beyond.activezone.expand.failed"), false);
        } else if (r >= minR) {
            syncLD(level);
        }
    }

    /** 在种子区块周围主动扫描节点结构，预注册节点区域（无需等待区块加载）。 */
    private void discoverNearbyStructures(ServerLevel level, Set<ChunkPos> seeds) {
        int scanRadius = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();
        for (ChunkPos seed : seeds) {
            BlockPos center = seed.getMiddleBlockPosition(0);
            for (int pass = 0; pass < 20; pass++) {
                var pos = level.findNearestMapStructure(NODE_STRUCTURE_TAG, center, scanRadius, false);
                if (pos == null) break;
                addNodeZone(level, pos);
                center = pos.offset(16, 0, 16);
            }
        }
    }

    // ---- 辅助 ----

    private Set<Set<ChunkPos>> groupComponents(Set<ChunkPos> uncompleted) {
        Set<Set<ChunkPos>> comps = new HashSet<>();
        Set<ChunkPos> remaining = new HashSet<>(uncompleted);
        while (!remaining.isEmpty()) comps.add(extractComponent(remaining));
        return comps;
    }

    private static Set<ChunkPos> extractComponent(Set<ChunkPos> remaining) {
        Set<ChunkPos> comp = new HashSet<>();
        ChunkPos seed = remaining.iterator().next();
        remaining.remove(seed);
        comp.add(seed);
        Deque<ChunkPos> q = new ArrayDeque<>();
        q.add(seed);
        while (!q.isEmpty()) {
            for (ChunkPos nb : neighbors8(q.poll())) {
                if (remaining.remove(nb)) { comp.add(nb); q.add(nb); }
            }
        }
        return comp;
    }

    private static Set<ChunkPos> getUncompletedNodes(ServerLevel level, Set<ChunkPos> nodeChunks) {
        var rogueData = BeyondAPI.getRogueData(level);
        Set<ChunkPos> uncompleted = new HashSet<>(nodeChunks);
        for (NodeData nd : rogueData.getNodeDatas()) {
            if (nd.getPhase() == NodePhase.UNLOCKED) {
                for (long v : nd.getNodeChunks())
                    uncompleted.remove(new ChunkPos(ChunkPos.getX(v), ChunkPos.getZ(v)));
            }
        }
        return uncompleted;
    }

    private static List<ChunkPos> neighbors8(ChunkPos p) {
        int cx = p.getMinBlockX() >> 4, cz = p.getMinBlockZ() >> 4;
        return List.of(
                new ChunkPos(cx+1,cz), new ChunkPos(cx-1,cz), new ChunkPos(cx,cz+1), new ChunkPos(cx,cz-1),
                new ChunkPos(cx+1,cz+1), new ChunkPos(cx-1,cz-1), new ChunkPos(cx+1,cz-1), new ChunkPos(cx-1,cz+1));
    }
}
