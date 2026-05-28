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
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.async.AsyncZoneExpansionService;
import org.galaxy.beyond.api.system.zone.async.ZoneExpansionJobType;
import org.galaxy.beyond.api.system.zone.async.ZoneExpansionRequest;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

import java.util.*;

public class ZoneManager implements IZoneManager {

    private final ZoneWriter writer = new ZoneWriter();
    private final ZoneConflictResolver conflictResolver = new ZoneConflictResolver(writer);
    private final NodeColorPicker nodeColorPicker = new NodeColorPicker();
    private final SafeZoneRegistrar safeZoneRegistrar = new SafeZoneRegistrar(writer, conflictResolver);
    private final NodeZoneRegistrar nodeZoneRegistrar = new NodeZoneRegistrar(writer, conflictResolver, nodeColorPicker);
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

    private LevelZoneData getLZD(ServerLevel l) {
        return BeyondAPI.getLevelZoneData(l);
    }

    @Override
    public void tick(ServerLevel level) {
        asyncExpansion.tick(level);
    }

    @Override
    public boolean addZone(ServerLevel level, ChunkPos pos, ZoneType type) {
        return writer.addZone(level, pos, type);
    }

    // ---- Safe Zone ----

    @Override
    public void addSafeZone(ServerLevel level, int chunkSize, BlockPos center) {
        safeZoneRegistrar.addSafeZone(level, chunkSize, center);
    }

    // ---- Node Zone ----

    @Override
    public void addNodeZone(ServerLevel level, BlockPos pos) {
        nodeZoneRegistrar.addNodeZone(level, pos);
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
