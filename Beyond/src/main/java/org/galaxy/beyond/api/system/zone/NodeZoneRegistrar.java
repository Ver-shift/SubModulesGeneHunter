package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NodeZoneRegistrar {

    private static final TagKey<Structure> NODE_STRUCTURE_TAG =
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("beyond", "node_structure"));

    private final ZoneWriter writer;
    private final ZoneConflictResolver conflictResolver;
    private final NodeColorPicker colorPicker;

    public NodeZoneRegistrar(ZoneWriter writer, ZoneConflictResolver conflictResolver, NodeColorPicker colorPicker) {
        this.writer = writer;
        this.conflictResolver = conflictResolver;
        this.colorPicker = colorPicker;
    }

    public NodeData addNodeZone(ServerLevel level, BlockPos pos) {
        var structureManager = BeyondAPI.getBeyondManager().getStructureManager();
        List<ChunkPos> chunks = structureManager.getStructureChunks(level, pos, NODE_STRUCTURE_TAG);
        if (chunks.isEmpty()) chunks = List.of(new ChunkPos(pos));
        LevelZoneData levelZoneData = BeyondAPI.getLevelZoneData(level);
        Set<ChunkPos> targets = new LinkedHashSet<>();
        for (ChunkPos chunk : chunks) {
            if (chunk == null) continue;
            if (levelZoneData.isSafe(chunk)) return null;
            targets.add(chunk);
        }
        if (targets.isEmpty()) return null;

        Set<NodeData> existingNodes = findExistingNodes(level, targets);
        if (!existingNodes.isEmpty()) return mergeExistingNodes(level, targets, existingNodes);
        if (!conflictResolver.clearLockedNodeConflicts(level, targets)) return null;

        var nodeData = new NodeData(colorPicker.random(level));
        for (ChunkPos chunk : targets) nodeData.addChunkPos(chunk);
        writer.registerNodeZone(level, targets, nodeData);
        return nodeData;
    }

    private static Set<NodeData> findExistingNodes(ServerLevel level, Set<ChunkPos> targets) {
        Set<NodeData> result = new LinkedHashSet<>();
        for (ChunkPos target : targets) {
            NodeData nodeData = BeyondAPI.findNodeData(level, target);
            if (nodeData != null) result.add(nodeData);
        }
        return result;
    }

    private NodeData mergeExistingNodes(ServerLevel level, Set<ChunkPos> targets, Set<NodeData> existingNodes) {
        NodeData kept = existingNodes.stream()
                .min(Comparator.comparingLong(NodeData::ensureNodeKey))
                .orElse(null);
        if (kept == null) return null;

        boolean locked = true;
        for (NodeData nodeData : existingNodes) {
            if (nodeData.getPhase() != NodePhase.LOCKED) {
                locked = false;
                break;
            }
        }
        if (locked) {
            for (NodeData nodeData : existingNodes) {
                if (nodeData.ensureNodeKey() == kept.ensureNodeKey()) continue;
                writer.removePackedZoneChunks(level, ZoneType.Node_Zone, nodeData.getNodeChunks());
                writer.removeNodeData(level, nodeData.ensureNodeKey());
            }
            writer.addNodeChunks(level, kept, targets);
        }
        return kept;
    }
}
