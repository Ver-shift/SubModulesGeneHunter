package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActiveZoneController {

    private static final TagKey<Structure> NODE_STRUCTURE_TAG =
            TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("beyond", "node_structure"));

    private final AsyncZoneExpansionService asyncExpansion;

    public ActiveZoneController(AsyncZoneExpansionService asyncExpansion) {
        this.asyncExpansion = asyncExpansion;
    }

    public void activeZoneInit(ServerLevel level) {
        LevelZoneData levelZoneData = BeyondAPI.getLevelZoneData(level);
        Set<Long> safe = new HashSet<>(levelZoneData.getPacked(ZoneType.Safe_Zone));
        if (safe.isEmpty()) return;

        int minRadius = CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get();
        long seed = centerOf(safe);
        int minNodes = CommonConfig.ACTIVE_ZONE_MIN_NODES.get();
        discoverNodeStructures(level, Set.of(seed), minNodes, maxRadius);
        Set<Long> targets = getExpansionTargetNodeChunks(level, Set.of(seed), maxRadius);
        logTargetScan(ZoneExpansionJobType.INITIAL, Set.of(seed), minNodes, maxRadius, targets,
                countExpansionTargetNodeAreas(level, Set.of(seed), maxRadius));
        asyncExpansion.clear(level);
        submitExpansion(level, ZoneExpansionJobType.INITIAL, Set.of(seed), targets,
                minNodes,
                minRadius,
                maxRadius);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.active_zone_initial_radius", minRadius), false);
    }

    public void addActiveZone(ServerLevel level, RogueNodeData nodeData) {
        Set<Long> seeds = new HashSet<>(nodeData.getNodeData().getNodeChunks());
        if (seeds.isEmpty()) return;

        long seed = centerOf(seeds);
        int minConnections = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();
        int minRadius = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();
        discoverNodeStructures(level, Set.of(seed), minConnections, maxRadius);
        Set<Long> targets = getExpansionTargetNodeChunks(level, Set.of(seed), maxRadius);
        logTargetScan(ZoneExpansionJobType.NODE_UNLOCK, Set.of(seed), minConnections, maxRadius, targets,
                countExpansionTargetNodeAreas(level, Set.of(seed), maxRadius));
        boolean submitted = submitExpansion(level, ZoneExpansionJobType.NODE_UNLOCK, Set.of(seed), targets,
                minConnections,
                minRadius,
                maxRadius);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(submitted ? "beyond.node.zone_expanding" : "beyond.node.zone_expanding_busy"), false);
    }

    public boolean expandFromWorldSeed(ServerLevel level, ChunkPos pos) {
        int minConnections = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();
        int minRadius = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();
        Set<Long> seeds = Set.of(PackedChunkPos.pack(pos));
        discoverNodeStructures(level, seeds, minConnections, maxRadius);
        Set<Long> targets = getExpansionTargetNodeChunks(level, seeds, maxRadius);
        logTargetScan(ZoneExpansionJobType.WORLD_SEED, seeds, minConnections, maxRadius, targets,
                countExpansionTargetNodeAreas(level, seeds, maxRadius));
        boolean submitted = submitExpansion(level, ZoneExpansionJobType.WORLD_SEED, seeds, targets,
                minConnections,
                minRadius,
                maxRadius);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(submitted ? "beyond.item.world_seed.expanding" : "beyond.node.zone_expanding_busy"), false);
        return submitted;
    }

    private static void discoverNodeStructures(ServerLevel level, Set<Long> seeds, int needed, int maxRadius) {
        int before = countExpansionTargetNodeAreas(level, seeds, maxRadius);
        if (before >= needed) return;

        List<BlockPos> probes = discoveryProbes(seeds, maxRadius);
        for (BlockPos probe : probes) {
            if (countExpansionTargetNodeAreas(level, seeds, maxRadius) >= needed) break;

            BlockPos nearest = level.findNearestMapStructure(NODE_STRUCTURE_TAG, probe, maxRadius, false);
            if (nearest == null) continue;
            if (minDistanceToSeeds(seeds, PackedChunkPos.pack(nearest.getX() >> 4, nearest.getZ() >> 4)) > maxRadius) {
                continue;
            }
            BeyondAPI.getBeyondManager().getZoneManager().addNodeZone(level, nearest);
        }
    }

    private static List<BlockPos> discoveryProbes(Set<Long> seeds, int maxRadius) {
        int half = Math.max(1, maxRadius / 2);
        int[][] offsets = {
                {0, 0},
                {half, 0}, {-half, 0}, {0, half}, {0, -half},
                {half, half}, {-half, -half}, {half, -half}, {-half, half},
                {maxRadius, 0}, {-maxRadius, 0}, {0, maxRadius}, {0, -maxRadius},
                {maxRadius, maxRadius}, {-maxRadius, -maxRadius}, {maxRadius, -maxRadius}, {-maxRadius, maxRadius}
        };

        List<BlockPos> probes = new ArrayList<>();
        for (long seed : seeds) {
            int seedX = PackedChunkPos.x(seed);
            int seedZ = PackedChunkPos.z(seed);
            for (int[] offset : offsets) {
                probes.add(new BlockPos((seedX + offset[0]) << 4, 0, (seedZ + offset[1]) << 4));
            }
        }
        return probes;
    }

    private static int countExpansionTargetNodeAreas(ServerLevel level, Set<Long> seeds, int maxRadius) {
        int count = 0;
        for (NodeData nodeData : BeyondAPI.getNodeDatas(level)) {
            if (isExpansionTarget(nodeData, seeds, maxRadius)) count++;
        }
        return count;
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
        return asyncExpansion.submit(level, type, request);
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

    private static Set<Long> getExpansionTargetNodeChunks(ServerLevel level, Set<Long> seeds, int maxRadius) {
        Set<Long> targets = new HashSet<>();
        for (NodeData nodeData : BeyondAPI.getNodeDatas(level)) {
            if (isExpansionTarget(nodeData, seeds, maxRadius)) targets.addAll(nodeData.getNodeChunks());
        }
        return targets;
    }

    private static boolean isExpansionTarget(NodeData nodeData, Set<Long> seeds, int maxRadius) {
        return nodeData.getPhase() != NodePhase.UNLOCKED
                && !nodeData.getNodeChunks().isEmpty()
                && minDistanceToSeeds(seeds, nodeData.getNodeChunks()) <= maxRadius;
    }

    private static int minDistanceToSeeds(Set<Long> seeds, List<Long> chunks) {
        int minDistance = Integer.MAX_VALUE;
        for (long chunk : chunks) {
            minDistance = Math.min(minDistance, minDistanceToSeeds(seeds, chunk));
        }
        return minDistance;
    }

    private static int minDistanceToSeeds(Set<Long> seeds, long chunk) {
        int chunkX = PackedChunkPos.x(chunk);
        int chunkZ = PackedChunkPos.z(chunk);
        double minDistance = Double.MAX_VALUE;
        for (long seed : seeds) {
            double dx = chunkX - PackedChunkPos.x(seed);
            double dz = chunkZ - PackedChunkPos.z(seed);
            minDistance = Math.min(minDistance, Math.sqrt(dx * dx + dz * dz));
        }
        return (int) Math.ceil(minDistance);
    }

    private static void logTargetScan(ZoneExpansionJobType type, Set<Long> seeds, int needed, int maxRadius,
                                      Set<Long> targets, int targetAreas) {
        Beyond.profileInfo(
                "[Zone][TARGET_SCAN] type={}, seeds={}, targetChunks={}, targetAreas={}, needed={}, maxRadius={}",
                type, seeds.size(), targets.size(), targetAreas, needed, maxRadius
        );
    }
}
