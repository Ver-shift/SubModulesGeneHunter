package org.galaxy.beyond.api.system.zone;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;

import java.util.HashSet;
import java.util.Set;

public class ZoneConflictResolver {

    private final ZoneWriter writer;

    public ZoneConflictResolver(ZoneWriter writer) {
        this.writer = writer;
    }

    public boolean clearLockedNodeConflicts(ServerLevel level, Set<ChunkPos> targets) {
        Set<NodeData> conflicts = findConflictingNodes(level, targets);
        for (NodeData nodeData : conflicts) {
            if (nodeData.getPhase() != NodePhase.LOCKED) return false;
        }
        for (NodeData nodeData : conflicts) {
            writer.removePackedZoneChunks(level, ZoneType.Node_Zone, nodeData.getNodeChunks());
            writer.removeNodeData(level, nodeData.ensureNodeKey());
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
}
