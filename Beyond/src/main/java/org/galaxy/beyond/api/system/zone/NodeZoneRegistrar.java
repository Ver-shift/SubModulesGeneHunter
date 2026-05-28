package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NodeZoneRegistrar {

    private final ZoneWriter writer;
    private final ZoneConflictResolver conflictResolver;
    private final NodeColorPicker colorPicker;

    public NodeZoneRegistrar(ZoneWriter writer, ZoneConflictResolver conflictResolver, NodeColorPicker colorPicker) {
        this.writer = writer;
        this.conflictResolver = conflictResolver;
        this.colorPicker = colorPicker;
    }

    public void addNodeZone(ServerLevel level, BlockPos pos) {
        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = structureManager.getStructureChunks(level, pos);
        LevelZoneData levelZoneData = BeyondAPI.getLevelZoneData(level);
        Set<ChunkPos> targets = new LinkedHashSet<>();
        for (ChunkPos chunk : chunks) {
            if (chunk == null) continue;
            if (levelZoneData.isSafe(chunk)) return;
            targets.add(chunk);
        }
        if (targets.isEmpty()) return;

        if (isRegisteredNode(level, targets)) return;
        if (!conflictResolver.clearLockedNodeConflicts(level, targets)) return;

        var nodeData = new NodeData(colorPicker.random(level));
        for (ChunkPos chunk : targets) nodeData.addChunkPos(chunk);
        writer.registerNodeZone(level, targets, nodeData);
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
}
