package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
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
import java.util.concurrent.CompletableFuture;

public class ActiveZoneController {

    private static final TagKey<Structure> NODE_STRUCTURE_TAG =
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("beyond", "node_structure"));

    private final AsyncZoneExpansionService asyncExpansion;
    private final NodeZoneRegistrar nodeZoneRegistrar;

    public ActiveZoneController(AsyncZoneExpansionService asyncExpansion, NodeZoneRegistrar nodeZoneRegistrar) {
        this.asyncExpansion = asyncExpansion;
        this.nodeZoneRegistrar = nodeZoneRegistrar;
    }

    public void activeZoneInit(ServerLevel level) {
        LevelZoneData levelZoneData = BeyondAPI.getLevelZoneData(level);
        Set<Long> safe = new HashSet<>(levelZoneData.getPacked(ZoneType.Safe_Zone));
        if (safe.isEmpty()) return;

        int minRadius = CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get();
        long seed = centerOf(safe);
        int minNodes = CommonConfig.ACTIVE_ZONE_MIN_NODES.get();
        Set<Long> active = new HashSet<>(levelZoneData.getPacked(ZoneType.Active_Zone));
        Set<Long> uncompleted = getUncompletedNodeChunks(level);
        uncompleted.addAll(locateNodeStructureChunks(level, Set.of(seed), minNodes, maxRadius, active));
        asyncExpansion.clear(level);
        submitExpansion(level, ZoneExpansionJobType.INITIAL, Set.of(seed), uncompleted,
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
        Set<Long> active = new HashSet<>(BeyondAPI.getLevelZoneData(level).getPacked(ZoneType.Active_Zone));
        Set<Long> uncompleted = getUncompletedNodeChunks(level);
        uncompleted.addAll(locateNodeStructureChunks(level, Set.of(seed), minConnections, maxRadius, active));
        boolean submitted = submitExpansion(level, ZoneExpansionJobType.NODE_UNLOCK, Set.of(seed), uncompleted,
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
        Set<Long> active = new HashSet<>(BeyondAPI.getLevelZoneData(level).getPacked(ZoneType.Active_Zone));
        Set<Long> uncompleted = getUncompletedNodeChunks(level);
        uncompleted.addAll(locateNodeStructureChunks(level, seeds, minConnections, maxRadius, active));
        boolean submitted = submitExpansion(level, ZoneExpansionJobType.WORLD_SEED, seeds, uncompleted,
                minConnections,
                minRadius,
                maxRadius);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(submitted ? "beyond.item.world_seed.expanding" : "beyond.node.zone_expanding_busy"), false);
        return submitted;
    }

    public CompletableFuture<NodeData> discoverNearestNode(ServerLevel level, ChunkPos center, int radius) {
        return CompletableFuture.supplyAsync(() -> findNearestPotentialNodeChunk(level, center, radius))
                .thenCompose(target -> target == null ? CompletableFuture.completedFuture(null) : loadStructureChunk(level, target))
                .thenApplyAsync(chunk -> chunk == null ? null
                        : nodeZoneRegistrar.addNodeZone(level, chunk.getPos().getWorldPosition()), level.getServer());
    }

    private static CompletableFuture<ChunkAccess> loadStructureChunk(ServerLevel level, ChunkPos pos) {
        return CompletableFuture.supplyAsync(() -> level.getChunkSource()
                        .getChunkFuture(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS, true))
                .thenCompose(future -> future)
                .thenApply(result -> result.orElse(null));
    }

    private static ChunkPos findNearestPotentialNodeChunk(ServerLevel level, ChunkPos center, int radius) {
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var tag = registry.getTag(NODE_STRUCTURE_TAG);
        if (tag.isEmpty()) return null;

        var state = level.getChunkSource().getGeneratorState();
        ChunkPos best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Holder<Structure> holder : tag.get()) {
            for (StructurePlacement placement : state.getPlacementsForStructure(holder)) {
                if (!(placement instanceof RandomSpreadStructurePlacement spread)) continue;
                ChunkPos target = nearestSpreadNodeChunk(level, tag.get(), spread, center.x, center.z, radius);
                if (target == null) continue;
                int distance = chunkDistance(center, target);
                if (distance < bestDistance) {
                    best = target;
                    bestDistance = distance;
                }
            }
        }
        return best;
    }

    private static ChunkPos nearestSpreadNodeChunk(ServerLevel level, HolderSet.Named<Structure> structures,
                                                   RandomSpreadStructurePlacement placement,
                                                   int centerX, int centerZ, int radius) {
        var state = level.getChunkSource().getGeneratorState();
        ChunkPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int r = 0; r <= radius; r++) {
            boolean found = false;
            for (int dz = -r; dz <= r; dz++) {
                for (int dx = -r; dx <= r; dx++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    ChunkPos candidate = placement.getPotentialStructureChunk(state.getLevelSeed(),
                            centerX + placement.spacing() * dx,
                            centerZ + placement.spacing() * dz);
                    if (!hasNodeStructure(level, structures, placement, candidate)) continue;
                    found = true;
                    int distance = chunkDistance(new ChunkPos(centerX, centerZ), candidate);
                    if (distance < bestDistance) {
                        best = candidate;
                        bestDistance = distance;
                    }
                }
            }
            if (found) return best;
        }
        return null;
    }

    private static boolean hasNodeStructure(ServerLevel level, HolderSet.Named<Structure> structures,
                                            StructurePlacement placement, ChunkPos pos) {
        var structureManager = level.structureManager();
        for (Holder<Structure> holder : structures) {
            StructureCheckResult result = structureManager.checkStructurePresence(pos,
                    holder.value(), placement, false);
            if (result == StructureCheckResult.START_PRESENT || result == StructureCheckResult.CHUNK_LOAD_NEEDED)
                return true;
        }
        return false;
    }

    private static int chunkDistance(ChunkPos from, ChunkPos to) {
        int dx = from.x - to.x;
        int dz = from.z - to.z;
        return (int) Math.ceil(Math.sqrt(dx * dx + dz * dz));
    }

    private Set<Long> locateNodeStructureChunks(ServerLevel level, Set<Long> seeds, int needed, int maxRadius, Set<Long> active) {
        int missing = needed - countNewUncompletedNodeAreas(level, active);
        if (missing <= 0) return Set.of();

        Set<Long> located = new HashSet<>();
        Set<Long> locatedNodes = new HashSet<>();
        List<BlockPos> probes = discoveryProbes(seeds, maxRadius);
        for (BlockPos probe : probes) {
            BlockPos nearest = level.findNearestMapStructure(NODE_STRUCTURE_TAG, probe, maxRadius, false);
            if (nearest == null) continue;
            NodeData nodeData = nodeZoneRegistrar.addNodeZone(level, nearest);
            if (nodeData == null) {
                long fallback = PackedChunkPos.pack(new ChunkPos(nearest));
                located.add(fallback);
                continue;
            }
            long nodeKey = nodeData.ensureNodeKey();
            if (locatedNodes.add(nodeKey)) {
                located.addAll(nodeData.getNodeChunks());
            }
        }
        return located;
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

    private static int countNewUncompletedNodeAreas(ServerLevel level, Set<Long> active) {
        int count = 0;
        for (NodeData nodeData : BeyondAPI.getNodeDatas(level)) {
            if (nodeData.getPhase() != NodePhase.UNLOCKED
                    && !nodeData.getNodeChunks().isEmpty()
                    && !touchesActive(nodeData.getNodeChunks(), active)) {
                count++;
            }
        }
        return count;
    }

    private static boolean touchesActive(Iterable<Long> chunks, Set<Long> active) {
        for (long chunk : chunks) {
            if (active.contains(chunk)) return true;
            for (long neighbor : neighbors4(chunk)) {
                if (active.contains(neighbor)) return true;
            }
        }
        return false;
    }

    private static List<Long> neighbors4(long chunk) {
        int x = PackedChunkPos.x(chunk);
        int z = PackedChunkPos.z(chunk);
        return List.of(
                PackedChunkPos.pack(x + 1, z),
                PackedChunkPos.pack(x - 1, z),
                PackedChunkPos.pack(x, z + 1),
                PackedChunkPos.pack(x, z - 1)
        );
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
