package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.async.AsyncZoneExpansionService;
import org.galaxy.beyond.api.system.zone.async.ZoneExpansionJobType;
import org.galaxy.beyond.api.system.zone.async.ZoneExpansionRequest;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;

public class ZoneManager implements IZoneManager {

    private final AsyncZoneExpansionService asyncExpansion = new AsyncZoneExpansionService();

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
    public void tick(ServerLevel level) {
        asyncExpansion.tick(level);
    }

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
        if (isRegisteredNode(level, targets)) {
            if (largeData.removePackedZoneChunks(ZoneType.Active_Zone, packChunks(targets))) syncLD(level);
            return;
        }
        if (!clearLockedConflicts(level, largeData, targets)) return;

        boolean activeChanged = largeData.removePackedZoneChunks(ZoneType.Active_Zone, packChunks(targets));
        boolean zoneChanged = largeData.addZoneChunks(ZoneType.Node_Zone, targets);

        var nd = new NodeData(randomNodeColor(level));
        for (ChunkPos cp : targets) nd.addChunkPos(cp);
        boolean nodeChanged = largeData.addNodeData(nd);

        if (activeChanged || zoneChanged || nodeChanged) {
            syncLD(level);
        }
    }

    private static List<Long> packChunks(Collection<ChunkPos> chunks) {
        List<Long> packed = new ArrayList<>(chunks.size());
        for (ChunkPos chunk : chunks) {
            packed.add(((long) chunk.x & 0xFFFFFFFFL) | (((long) chunk.z & 0xFFFFFFFFL) << 32));
        }
        return packed;
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
        Set<Long> seeds = new HashSet<>(lzd.getPacked(ZoneType.Safe_Zone));
        if (seeds.isEmpty()) return;

        submitExpansion(level, ZoneExpansionJobType.INITIAL, seeds, getUncompletedNodeChunks(level),
                CommonConfig.ACTIVE_ZONE_MIN_NODES.get(),
                CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get(),
                CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get());
    }

    @Override
    public void addActiveZone(ServerLevel level, RogueNodeData nodeData) {
        Set<Long> seeds = new HashSet<>(nodeData.getNodeData().getNodeChunks());
        if (seeds.isEmpty()) return;

        Beyond.debugInfo(
                "[Zone][NODE_UNLOCK] seeds={}, minConnections={}, minRadius={}, maxRadius={}, nodeKey={}, nodePhase={}",
                seeds.size(),
                CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get(),
                CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get(),
                CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get(),
                nodeData.getNodeData().ensureNodeKey(),
                nodeData.getNodeData().getPhase().name()
        );

        boolean submitted = submitExpansion(level, ZoneExpansionJobType.NODE_UNLOCK, seeds, getUncompletedNodeChunks(level),
                CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get(),
                CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get(),
                CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get());
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(submitted ? "beyond.node.zone_expanding" : "beyond.node.zone_expanding_busy"), false);
    }

    // ---- 辅助 ----

    private boolean submitExpansion(ServerLevel level, ZoneExpansionJobType type, Set<Long> seeds, Set<Long> uncompleted,
                                    int minConnections, int minRadius, int maxRadius) {
        LevelZoneData data = getLZD(level);
        long jobId = asyncExpansion.nextJobId();
        ZoneExpansionRequest request = new ZoneExpansionRequest(
                jobId,
                Set.copyOf(seeds),
                new HashSet<>(data.getPacked(ZoneType.Safe_Zone)),
                new HashSet<>(data.getPacked(ZoneType.Node_Zone)),
                new HashSet<>(data.getPacked(ZoneType.Active_Zone)),
                Set.copyOf(uncompleted),
                minConnections,
                minRadius,
                maxRadius
        );
        return asyncExpansion.submit(level, type, request);
    }

    private static Set<Long> getUncompletedNodeChunks(ServerLevel level) {
        Set<Long> uncompleted = new HashSet<>(BeyondAPI.getLevelZoneData(level).getPacked(ZoneType.Node_Zone));
        for (NodeData nd : BeyondAPI.getNodeDatas(level)) {
            if (nd.getPhase() == NodePhase.UNLOCKED) {
                uncompleted.removeAll(nd.getNodeChunks());
            }
        }
        return uncompleted;
    }
}
