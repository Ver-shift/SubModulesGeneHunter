package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveZoneController {

    private static final int PROGRESS_INTERVAL = 20;
    private static final int NODE_REGISTRATION_PER_TICK = 1;
    private static final String[] SPINNER = {"|", "/", "-", "\\"};

    private final AsyncZoneExpansionService asyncExpansion;
    private final NodeZoneRegistrar nodeZoneRegistrar;
    private final Map<ResourceKey<Level>, ZoneExpansionJobType> scanning = new ConcurrentHashMap<>();
    private final Map<ResourceKey<Level>, PendingNodeRegistration> registering = new ConcurrentHashMap<>();

    public ActiveZoneController(AsyncZoneExpansionService asyncExpansion, NodeZoneRegistrar nodeZoneRegistrar) {
        this.asyncExpansion = asyncExpansion;
        this.nodeZoneRegistrar = nodeZoneRegistrar;
    }

    public void tick(ServerLevel level) {
        if (!CommonConfig.isRogueDimension(level)) return;

        ResourceKey<Level> key = level.dimension();
        PendingNodeRegistration registration = registering.get(key);
        if (registration != null) {
            tickRegistration(level, key, registration);
            return;
        }

        ZoneExpansionJobType type = scanning.get(key);
        if (type == null) return;
        int ticks = level.getServer().getTickCount();
        if (ticks % PROGRESS_INTERVAL != 0) return;
        showProgress(level, Component.translatable(progressScanKey(type), spinner(ticks)));
    }

    public void activeZoneInit(ServerLevel level) {
        if (!CommonConfig.isRogueDimension(level)) return;

        LevelZoneData levelZoneData = BeyondAPI.getLevelZoneData(level);
        Set<Long> safe = new HashSet<>(levelZoneData.getPacked(ZoneType.Safe_Zone));
        if (safe.isEmpty()) return;

        int minRadius = CommonConfig.ACTIVE_ZONE_MIN_EXPAND.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_MAX_EXPAND.get();
        long seed = centerOf(safe);
        int minNodes = CommonConfig.ACTIVE_ZONE_MIN_NODES.get();
        Set<Long> active = new HashSet<>(levelZoneData.getPacked(ZoneType.Active_Zone));
        asyncExpansion.clear(level);
        scheduleExpansion(level, ZoneExpansionJobType.INITIAL, Set.of(seed), active,
                minNodes,
                minRadius,
                maxRadius);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.active_zone_initial_radius", minRadius), false);
    }

    public void addActiveZone(ServerLevel level, RogueNodeData nodeData) {
        if (!CommonConfig.isRogueDimension(level)) return;

        Set<Long> seeds = new HashSet<>(nodeData.getNodeData().getNodeChunks());
        if (seeds.isEmpty()) return;

        long seed = centerOf(seeds);
        int minConnections = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();
        int minRadius = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();
        Set<Long> active = new HashSet<>(BeyondAPI.getLevelZoneData(level).getPacked(ZoneType.Active_Zone));
        boolean submitted = scheduleExpansion(level, ZoneExpansionJobType.NODE_UNLOCK, Set.of(seed), active,
                minConnections,
                minRadius,
                maxRadius);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(submitted ? "beyond.node.zone_expanding" : "beyond.node.zone_expanding_busy"), false);
    }

    public boolean expandFromWorldSeed(ServerLevel level, ChunkPos pos) {
        if (!CommonConfig.isRogueDimension(level)) return false;

        int minConnections = CommonConfig.ACTIVE_ZONE_MIN_CONNECTIONS.get();
        int minRadius = CommonConfig.ACTIVE_ZONE_NODE_EXPAND_RADIUS.get();
        int maxRadius = CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get();
        Set<Long> seeds = Set.of(PackedChunkPos.pack(pos));
        Set<Long> active = new HashSet<>(BeyondAPI.getLevelZoneData(level).getPacked(ZoneType.Active_Zone));
        boolean submitted = scheduleExpansion(level, ZoneExpansionJobType.WORLD_SEED, seeds, active,
                minConnections,
                minRadius,
                maxRadius);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(submitted ? "beyond.item.world_seed.expanding" : "beyond.node.zone_expanding_busy"), false);
        return submitted;
    }

    public CompletableFuture<NodeData> discoverNearestNode(ServerLevel level, ChunkPos center, int radius) {
        if (!CommonConfig.isRogueDimension(level)) return CompletableFuture.completedFuture(null);

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
        var rogueConfig = CommonConfig.getRogueDimensionConfig(level);
        if (rogueConfig.isEmpty()) return null;

        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var tag = registry.getTag(TagKey.create(Registries.STRUCTURE, rogueConfig.get().nodeStructureTag()));
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

    private static boolean hasNodeStructure(ServerLevel level, Holder<Structure> holder,
                                            StructurePlacement placement, ChunkPos pos) {
        StructureCheckResult result = level.structureManager().checkStructurePresence(pos,
                holder.value(), placement, false);
        return result == StructureCheckResult.START_PRESENT || result == StructureCheckResult.CHUNK_LOAD_NEEDED;
    }

    private static int chunkDistance(ChunkPos from, ChunkPos to) {
        int dx = from.x - to.x;
        int dz = from.z - to.z;
        return (int) Math.ceil(Math.sqrt(dx * dx + dz * dz));
    }

    private boolean scheduleExpansion(ServerLevel level, ZoneExpansionJobType type, Set<Long> seeds, Set<Long> active,
                                      int needed, int minRadius, int maxRadius) {
        ResourceKey<Level> key = level.dimension();
        if (asyncExpansion.hasWork(level) || registering.containsKey(key) || scanning.putIfAbsent(key, type) != null)
            return false;

        CompletableFuture
                .supplyAsync(() -> locateNodeStructureStarts(level, seeds, needed, maxRadius, active))
                .whenCompleteAsync((starts, throwable) -> {
                    scanning.remove(key);
                    if (throwable != null) {
                        Beyond.debugInfo("[Zone][SCAN_FAILED] type={}, error={}", type, throwable.toString());
                        level.getServer().getPlayerList().broadcastSystemMessage(
                                Component.translatable("commands.beyond.activezone.expand.failed"), false);
                        return;
                    }

                    PendingNodeRegistration registration = new PendingNodeRegistration(type, seeds,
                            needed, minRadius, maxRadius, sortedStarts(starts));
                    if (registration.isDone()) submitAfterRegistration(level, registration);
                    else registering.put(key, registration);
                }, level.getServer());
        return true;
    }

    private Set<ChunkPos> locateNodeStructureStarts(ServerLevel level, Set<Long> seeds, int needed,
                                                    int maxRadius, Set<Long> active) {
        var rogueConfig = CommonConfig.getRogueDimensionConfig(level);
        if (rogueConfig.isEmpty()) return Set.of();

        int missing = needed - countNewUncompletedNodeAreas(level, active);
        if (missing <= 0) return Set.of();

        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var tag = registry.getTag(TagKey.create(Registries.STRUCTURE, rogueConfig.get().nodeStructureTag()));
        if (tag.isEmpty()) return Set.of();

        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        Set<ChunkPos> located = new HashSet<>();
        for (Holder<Structure> holder : tag.get()) {
            for (StructurePlacement placement : state.getPlacementsForStructure(holder)) {
                scanPlacement(level, state, holder, placement, seeds, maxRadius, located);
            }
        }
        return located;
    }

    private static void scanPlacement(ServerLevel level, ChunkGeneratorStructureState state, Holder<Structure> holder,
                                      StructurePlacement placement, Set<Long> seeds, int maxRadius,
                                      Set<ChunkPos> located) {
        long radiusSqr = (long) maxRadius * maxRadius;
        for (long seed : seeds) {
            int centerX = PackedChunkPos.x(seed);
            int centerZ = PackedChunkPos.z(seed);
            for (int dx = -maxRadius; dx <= maxRadius; dx++) {
                long dxSqr = (long) dx * dx;
                int dzMax = (int) Math.sqrt(radiusSqr - dxSqr);
                for (int dz = -dzMax; dz <= dzMax; dz++) {
                    int x = centerX + dx;
                    int z = centerZ + dz;
                    if (!placement.isStructureChunk(state, x, z)) continue;
                    ChunkPos candidate = new ChunkPos(x, z);
                    if (hasNodeStructure(level, holder, placement, candidate)) located.add(candidate);
                }
            }
        }
    }

    private static List<ChunkPos> sortedStarts(Set<ChunkPos> starts) {
        return starts.stream()
                .sorted(Comparator.comparingInt((ChunkPos chunk) -> chunk.x).thenComparingInt(chunk -> chunk.z))
                .toList();
    }

    private void tickRegistration(ServerLevel level, ResourceKey<Level> key, PendingNodeRegistration registration) {
        int ticks = level.getServer().getTickCount();
        if (ticks % PROGRESS_INTERVAL == 0) {
            showProgress(level, Component.translatable(progressRegisterKey(registration.type),
                    spinner(ticks), registration.cursor(), registration.total()));
        }

        registration.apply(level, nodeZoneRegistrar);
        if (!registration.isDone()) return;

        registering.remove(key);
        submitAfterRegistration(level, registration);
    }

    private void submitAfterRegistration(ServerLevel level, PendingNodeRegistration registration) {
        Set<Long> uncompleted = getUncompletedNodeChunks(level);
        uncompleted.addAll(registration.located());
        boolean submitted = submitExpansion(level, registration.type(), registration.seeds(), uncompleted,
                registration.needed(), registration.minRadius(), registration.maxRadius());
        if (!submitted) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.zone_expanding_busy"), false);
        }
    }

    private static void registerNodeStructure(ServerLevel level, NodeZoneRegistrar nodeZoneRegistrar,
                                               ChunkPos start, Set<Long> located, Set<Long> locatedNodes) {
        NodeData nodeData = nodeZoneRegistrar.addNodeZone(level, List.of(start));
        if (nodeData == null) {
            located.add(PackedChunkPos.pack(start));
            return;
        }
        long nodeKey = nodeData.ensureNodeKey();
        if (locatedNodes.add(nodeKey)) {
            located.addAll(nodeData.getNodeChunks());
        }
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

    private static void showProgress(ServerLevel level, Component message) {
        for (var player : level.players()) {
            player.displayClientMessage(message, true);
        }
    }

    private static String progressScanKey(ZoneExpansionJobType type) {
        return type == ZoneExpansionJobType.INITIAL
                ? "beyond.node.world_initializing_scan"
                : "beyond.node.zone_expanding_scan";
    }

    private static String progressRegisterKey(ZoneExpansionJobType type) {
        return type == ZoneExpansionJobType.INITIAL
                ? "beyond.node.world_initializing_register"
                : "beyond.node.zone_expanding_register";
    }

    private static String spinner(int ticks) {
        return SPINNER[(ticks / PROGRESS_INTERVAL) % SPINNER.length];
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

    private static class PendingNodeRegistration {

        private final ZoneExpansionJobType type;
        private final Set<Long> seeds;
        private final int needed;
        private final int minRadius;
        private final int maxRadius;
        private final List<ChunkPos> starts;
        private final Set<Long> located = new HashSet<>();
        private final Set<Long> locatedNodes = new HashSet<>();
        private int cursor;

        private PendingNodeRegistration(ZoneExpansionJobType type, Set<Long> seeds, int needed,
                                        int minRadius, int maxRadius, List<ChunkPos> starts) {
            this.type = type;
            this.seeds = Set.copyOf(seeds);
            this.needed = needed;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.starts = new ArrayList<>(starts);
        }

        private void apply(ServerLevel level, NodeZoneRegistrar nodeZoneRegistrar) {
            int end = Math.min(cursor + NODE_REGISTRATION_PER_TICK, starts.size());
            for (; cursor < end; cursor++) {
                registerNodeStructure(level, nodeZoneRegistrar, starts.get(cursor), located, locatedNodes);
            }
        }

        private boolean isDone() {
            return cursor >= starts.size();
        }

        private ZoneExpansionJobType type() {
            return type;
        }

        private Set<Long> seeds() {
            return seeds;
        }

        private int needed() {
            return needed;
        }

        private int minRadius() {
            return minRadius;
        }

        private int maxRadius() {
            return maxRadius;
        }

        private Set<Long> located() {
            return located;
        }

        private int cursor() {
            return cursor;
        }

        private int total() {
            return starts.size();
        }
    }
}
