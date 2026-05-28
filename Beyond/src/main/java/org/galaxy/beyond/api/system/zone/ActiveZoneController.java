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

import java.util.HashSet;
import java.util.Set;

public class ActiveZoneController {

    private final AsyncZoneExpansionService asyncExpansion;

    public ActiveZoneController(AsyncZoneExpansionService asyncExpansion) {
        this.asyncExpansion = asyncExpansion;
    }

    public void activeZoneInit(ServerLevel level) {
        LevelZoneData levelZoneData = BeyondAPI.getLevelZoneData(level);
        Set<Long> seeds = new HashSet<>(levelZoneData.getPacked(ZoneType.Safe_Zone));
        if (seeds.isEmpty()) return;

        submitExpansion(level, ZoneExpansionJobType.INITIAL, seeds, getUncompletedNodeChunks(level),
                CommonConfig.ACTIVE_ZONE_MIN_NODES.get(),
                CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get(),
                CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get());
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

        boolean submitted = submitExpansion(level, ZoneExpansionJobType.NODE_UNLOCK, seeds, getUncompletedNodeChunks(level),
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
        for (NodeData nodeData : BeyondAPI.getNodeDatas(level)) {
            if (nodeData.getPhase() == NodePhase.UNLOCKED) {
                uncompleted.removeAll(nodeData.getNodeChunks());
            }
        }
        return uncompleted;
    }
}
