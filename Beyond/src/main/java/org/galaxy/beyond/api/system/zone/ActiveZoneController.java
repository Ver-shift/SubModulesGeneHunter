package org.galaxy.beyond.api.system.zone;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.async.AsyncZoneExpansionService;
import org.galaxy.beyond.api.system.zone.async.ZoneExpansionJobType;
import org.galaxy.beyond.api.system.zone.async.ZoneExpansionRequest;
import org.galaxy.beyond.api.system.zone.util.PackedChunkPos;

import java.util.HashSet;
import java.util.Set;

public class ActiveZoneController {

    private final AsyncZoneExpansionService asyncExpansion;

    public ActiveZoneController(AsyncZoneExpansionService asyncExpansion) {
        this.asyncExpansion = asyncExpansion;
    }

    public void activeZoneInit(ServerLevel level) {
        LevelZoneData levelZoneData = BeyondAPI.getLevelZoneData(level);
        Set<Long> safe = new HashSet<>(levelZoneData.getPacked(ZoneType.Safe_Zone));
        if (safe.isEmpty()) return;

        int minRadius = CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
        asyncExpansion.clear(level);
        submitExpansion(level, ZoneExpansionJobType.INITIAL, Set.of(centerOf(safe)), getUncompletedNodeChunks(level),
                CommonConfig.ACTIVE_ZONE_MIN_NODES.get(),
                minRadius,
                CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get());
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.active_zone_initial_radius", minRadius), false);
    }

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

        boolean submitted = submitExpansion(level, ZoneExpansionJobType.NODE_UNLOCK, Set.of(centerOf(seeds)), getUncompletedNodeChunks(level),
                CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get(),
                CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get(),
                CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get());
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(submitted ? "beyond.node.zone_expanding" : "beyond.node.zone_expanding_busy"), false);
    }

    private boolean submitExpansion(ServerLevel level, ZoneExpansionJobType type, Set<Long> seeds, Set<Long> uncompleted,
                                    int minConnections, int minRadius, int maxRadius) {
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        long jobId = asyncExpansion.nextJobId();
        ZoneExpansionRequest request = new ZoneExpansionRequest(
                jobId,
                type,
                Set.copyOf(seeds),
                new HashSet<>(data.getPacked(ZoneType.Safe_Zone)),
                new HashSet<>(data.getPacked(ZoneType.Node_Zone)),
                new HashSet<>(data.getPacked(ZoneType.Active_Zone)),
                Set.copyOf(uncompleted),
                minConnections,
                minRadius,
                maxRadius
        );
        boolean submitted = asyncExpansion.submit(level, type, request);
        Beyond.debugInfo(
                "[Zone][EXPAND_SUBMIT] jobId={}, type={}, submitted={}, seeds={}, safe={}, node={}, active={}, uncompleted={}, minConnections={}, minRadius={}, maxRadius={}",
                jobId,
                type,
                submitted,
                seeds.size(),
                request.safe().size(),
                request.node().size(),
                request.active().size(),
                uncompleted.size(),
                minConnections,
                minRadius,
                maxRadius
        );
        return submitted;
    }

    private static long centerOf(Set<Long> chunks) {
        long totalX = 0;
        long totalZ = 0;
        for (long chunk : chunks) {
            totalX += PackedChunkPos.x(chunk);
            totalZ += PackedChunkPos.z(chunk);
        }
        return PackedChunkPos.pack(Math.round((float) totalX / chunks.size()), Math.round((float) totalZ / chunks.size()));
    }

    private static Set<Long> getUncompletedNodeChunks(ServerLevel level) {
        Set<Long> uncompleted = new HashSet<>(BeyondAPI.getLevelZoneData(level).getPacked(ZoneType.Node_Zone));
        for (NodeData nodeData : BeyondAPI.getNodeDatas(level)) {
            if (nodeData.getPhase() == NodePhase.UNLOCKED) {
                uncompleted.removeAll(nodeData.getNodeChunks());
            }
        }
        return uncompleted;
    }
}
