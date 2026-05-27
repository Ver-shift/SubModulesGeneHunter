package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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

public class ZoneManager implements IZoneManager {

    private final NodeAwareExpander nodeExpander = new NodeAwareExpander(this::groupComponents);

    private static final TagKey<Structure> NODE_STRUCTURE_TAG =
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("beyond", "node_structure"));

    @Override
    public void onChunkLoad(ChunkAccess chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel sl)) return;
        if (BeyondAPI.getSafeZoneStructureData(sl).getInitialized() < 1) return;
        var sm = BeyondAPI.getBeyondManager().getStructureManager();
        BlockPos wp = new BlockPos(chunk.getPos().x << 4, 0, chunk.getPos().z << 4);
        if (sm.hasStructureByTag(sl, wp, NODE_STRUCTURE_TAG)) addNodeZone(sl, wp);
    }

    private LevelZoneData getLZD(ServerLevel l) { return BeyondAPI.getLevelZoneData(l); }
    private void syncLD(ServerLevel l) { BeyondAPI.syncLargeLevelData(l); }

    @Override
    public boolean addZone(ServerLevel level, ChunkPos pos, ZoneType type) {
        boolean changed = BeyondAPI.getLargeLevelData(level).addZoneChunks(type, List.of(pos));
        if (changed) syncLD(level);
        return changed;
    }

    // ---- Safe Zone ----

    @Override
    public void addSafeZone(ServerLevel level, int chunkSize, BlockPos center) {
        ChunkPos cc = org.galaxy.beyond.api.util.CompatUtil.chunkPos(center);
        int radius = chunkSize / 2;
        Set<ChunkPos> targets = ZoneHelper.expandSquare(cc, radius);

        var largeData = BeyondAPI.getLargeLevelData(level);
        if (!clearLockedConflicts(level, largeData, targets)) return;

        boolean changed = largeData.addZoneChunks(ZoneType.Safe_Zone, targets);
        if (changed) syncLD(level);
    }

    // ---- Node Zone ----

    @Override
    public void addNodeZone(ServerLevel level, BlockPos pos) {
        var sm = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = sm.getStructureChunks(level, pos);
        LevelZoneData lzd = getLZD(level);
        Set<ChunkPos> targets = new LinkedHashSet<>();
        for (ChunkPos cp : chunks) {
            if (cp == null) continue;
            if (lzd.isSafe(cp)) return;
            targets.add(cp);
        }
        if (targets.isEmpty()) return;

        var largeData = BeyondAPI.getLargeLevelData(level);
        if (isRegisteredNode(level, targets)) return;
        if (!clearLockedConflicts(level, largeData, targets)) return;

        boolean zoneChanged = largeData.addZoneChunks(ZoneType.Node_Zone, targets);

        var nd = new NodeData(randomNodeColor(level));
        for (ChunkPos cp : targets) nd.addChunkPos(cp);
        boolean nodeChanged = largeData.addNodeData(nd);

        if (zoneChanged || nodeChanged) {
            syncLD(level);
        }
    }

    private static boolean isRegisteredNode(ServerLevel level, Set<ChunkPos> targets) {
        NodeData first = null;
        for (ChunkPos target : targets) {
            NodeData nodeData = BeyondAPI.findNodeData(level, target);
            if (nodeData == null) return false;
            if (first == null) first = nodeData;
            else if (first.ensureNodeKey() != nodeData.ensureNodeKey()) return false;
        }
        return first != null && new HashSet<>(first.getNodeChunkPosList()).equals(targets);
    }

    private static boolean clearLockedConflicts(ServerLevel level,
                                                org.galaxy.beyond.api.system.large.BeyondLargeLevelData largeData,
                                                Set<ChunkPos> targets) {
        Set<NodeData> conflicts = findConflictingNodes(level, targets);
        for (NodeData nodeData : conflicts) {
            if (nodeData.getPhase() != NodePhase.LOCKED) return false;
        }
        for (NodeData nodeData : conflicts) {
            largeData.removePackedZoneChunks(ZoneType.Node_Zone, nodeData.getNodeChunks());
            largeData.removeNodeData(nodeData.ensureNodeKey());
        }
        return true;
    }

    private static Set<NodeData> findConflictingNodes(ServerLevel level, Set<ChunkPos> targets) {
        Set<NodeData> conflicts = new HashSet<>();
        for (ChunkPos target : targets) {
            NodeData nodeData = BeyondAPI.findNodeData(level, target);
            if (nodeData != null) conflicts.add(nodeData);
        }
        return conflicts;
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

        List<ChunkPos> addedActive = new ArrayList<>();
        int r = nodeExpander.expandUntilWrapped(level, safeChunks, uncompleted,
                CommonConfig.ACTIVE_ZONE_MIN_NODES.get(), minR, maxR, lzd,
                p -> {
                    if (lzd.addActive(p)) {
                        addedActive.add(p);
                        return true;
                    }
                    return false;
                });
        if (!addedActive.isEmpty()) {
            BeyondAPI.getLargeLevelData(level).recordAddedZoneChunks(ZoneType.Active_Zone, addedActive);
        }
        if (r >= minR) syncLD(level);
    }

    @Override
    public void addActiveZone(ServerLevel level, RogueNodeData nodeData) {
        List<ChunkPos> list = nodeData.getNodeData().getNodeChunkPosList();
        if (list.isEmpty()) return;

        Set<ChunkPos> seeds = new HashSet<>(list);
        LevelZoneData lzd = getLZD(level);

        Set<ChunkPos> nodeChunks = lzd.nodeChunks();
        Set<ChunkPos> uncompleted = getUncompletedNodes(level, nodeChunks);

        int need = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();
        int minR = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int maxR = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();

        List<ChunkPos> addedActive = new ArrayList<>();
        int r = nodeExpander.expandUntilWrapped(level, seeds, uncompleted, need, minR, maxR, lzd,
                p -> {
                    if (lzd.addActive(p)) {
                        addedActive.add(p);
                        return true;
                    }
                    return false;
                });
        if (!addedActive.isEmpty()) {
            BeyondAPI.getLargeLevelData(level).recordAddedZoneChunks(ZoneType.Active_Zone, addedActive);
        }
        if (r < 0) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable("commands.beyond.activezone.expand.failed"), false);
        } else if (r >= minR) {
            syncLD(level);
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
        Set<ChunkPos> uncompleted = new HashSet<>(nodeChunks);
        for (NodeData nd : BeyondAPI.getNodeDatas(level)) {
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
