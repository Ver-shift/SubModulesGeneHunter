package org.galaxy.beyond.api.system.zone.async;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PackedZoneExpander {

    public ZoneExpansionResult expand(ZoneExpansionRequest request) {
        LongSet safe = copyOf(request.safe());
        LongSet node = copyOf(request.node());
        LongSet active = copyOf(request.active());
        LongLinkedOpenHashSet added = new LongLinkedOpenHashSet();

        LongSet beforeReachable = floodReachable(request.seeds(), safe, node, active);
        List<LongSet> targetAreas = targetAreasOutsideReachable(request.targetNodeAreas(), beforeReachable);

        int needed = targetAreas.isEmpty() ? 0 : Math.min(request.minConnections(), targetAreas.size());
        int actualRadius = -1;
        int reached = 0;
        int furthestReached = 0;
        TargetStats targetStats = analyzeTargets(request.seeds(), targetAreas);
        ZoneProfiler.logExpansionPlan(request.jobId(), countChunks(targetAreas), targetAreas.size(), needed,
                targetStats.nearestDistance(), targetStats.furthestDistance(), targetStats.largestComponent());

        for (int radius = 1; radius <= request.maxRadius(); radius++) {
            addRing(request.seeds(), radius, safe, node, active, added);

            if (radius < request.minRadius()) continue;
            if (needed == 0) {
                int explorationRadius = Math.min(Math.max(request.minRadius() * 2, 40), request.maxRadius());
                for (int extraRadius = radius + 1; extraRadius <= explorationRadius; extraRadius++) {
                    addRing(request.seeds(), extraRadius, safe, node, active, added);
                }
                actualRadius = explorationRadius;
                break;
            }

            LongSet reachable = floodReachable(request.seeds(), safe, node, active);
            reached = countReachedComponents(targetAreas, reachable);
            if (reached < needed) continue;

            furthestReached = furthestReachedDistance(request.seeds(), targetAreas, reachable);
            actualRadius = Math.min(Math.max(radius, furthestReached), request.maxRadius());
            for (int extraRadius = radius + 1; extraRadius <= actualRadius; extraRadius++) {
                addRing(request.seeds(), extraRadius, safe, node, active, added);
            }
            break;
        }

        boolean success = actualRadius >= 0;
        ZoneProfiler.logExpansionResult(request.jobId(), success, actualRadius, reached, needed,
                targetAreas.size(), added.size());
        return new ZoneExpansionResult(
                request.jobId(),
                toList(added),
                actualRadius,
                0,
                reached,
                targetAreas.size(),
                needed,
                targetStats.nearestDistance(),
                furthestReached,
                success
        );
    }

    private static List<LongSet> targetAreasOutsideReachable(List<Set<Long>> areas, LongSet reachable) {
        List<LongSet> targets = new ArrayList<>();
        for (Set<Long> area : areas) {
            LongSet copy = copyOf(area);
            if (copy.isEmpty() || intersects(copy, reachable)) continue;
            targets.add(copy);
        }
        return targets;
    }

    private static boolean intersects(LongSet chunks, LongSet other) {
        LongIterator iterator = chunks.iterator();
        while (iterator.hasNext()) {
            if (other.contains(iterator.nextLong())) return true;
        }
        return false;
    }

    private static int countChunks(List<LongSet> areas) {
        int count = 0;
        for (LongSet area : areas) count += area.size();
        return count;
    }

    private static int countReachedComponents(List<LongSet> components, LongSet reachable) {
        int reached = 0;
        for (LongSet component : components) {
            LongIterator iterator = component.iterator();
            while (iterator.hasNext()) {
                if (reachable.contains(iterator.nextLong())) {
                    reached++;
                    break;
                }
            }
        }
        return reached;
    }

    private static int furthestReachedDistance(Set<Long> seeds, List<LongSet> components, LongSet reachable) {
        int furthest = 0;
        for (LongSet component : components) {
            boolean reached = false;
            LongIterator iterator = component.iterator();
            while (iterator.hasNext()) {
                if (reachable.contains(iterator.nextLong())) {
                    reached = true;
                    break;
                }
            }
            if (!reached) continue;

            iterator = component.iterator();
            while (iterator.hasNext()) {
                furthest = Math.max(furthest, minDistanceToSeeds(seeds, iterator.nextLong()));
            }
        }
        return furthest;
    }

    private static TargetStats analyzeTargets(Set<Long> seeds, List<LongSet> components) {
        if (components.isEmpty()) return new TargetStats(-1, -1, 0);

        int nearest = Integer.MAX_VALUE;
        int furthest = 0;
        int largest = 0;
        for (LongSet component : components) {
            largest = Math.max(largest, component.size());
            LongIterator iterator = component.iterator();
            while (iterator.hasNext()) {
                int distance = minDistanceToSeeds(seeds, iterator.nextLong());
                nearest = Math.min(nearest, distance);
                furthest = Math.max(furthest, distance);
            }
        }
        return new TargetStats(nearest, furthest, largest);
    }

    private static LongSet floodReachable(Set<Long> seeds, LongSet safe, LongSet node, LongSet active) {
        LongOpenHashSet visited = new LongOpenHashSet();
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        for (long seed : seeds) {
            if (!hasAny(safe, node, active, seed)) continue;
            visited.add(seed);
            queue.enqueue(seed);
        }
        while (!queue.isEmpty()) {
            enqueueReachableNeighbors(queue.dequeueLong(), safe, node, active, visited, queue);
        }
        return visited;
    }

    private static void addRing(Set<Long> seeds, int radius, LongSet safe, LongSet node, LongSet active,
                                LongLinkedOpenHashSet added) {
        if (radius == 0) {
            for (long seed : seeds) addActive(seed, safe, node, active, added);
            return;
        }

        LongLinkedOpenHashSet outer = expandCircleMulti(seeds, radius);
        outer.removeAll(expandCircleMulti(seeds, radius - 1));
        LongIterator iterator = outer.iterator();
        while (iterator.hasNext()) {
            addActive(iterator.nextLong(), safe, node, active, added);
        }
    }

    private static LongLinkedOpenHashSet expandCircleMulti(Set<Long> seeds, int radius) {
        LongLinkedOpenHashSet result = new LongLinkedOpenHashSet();
        long radiusSqr = (long) radius * radius;
        for (long seed : seeds) {
            int centerX = x(seed);
            int centerZ = z(seed);
            for (int dx = -radius; dx <= radius; dx++) {
                long dxSqr = (long) dx * dx;
                int dzMax = (int) Math.sqrt(radiusSqr - dxSqr);
                for (int dz = -dzMax; dz <= dzMax; dz++) {
                    result.add(pack(centerX + dx, centerZ + dz));
                }
            }
        }
        return result;
    }

    private static void enqueueReachableNeighbors(long chunk, LongSet safe, LongSet node, LongSet active,
                                                  LongSet visited, LongArrayFIFOQueue queue) {
        int x = x(chunk);
        int z = z(chunk);
        enqueueReachable(pack(x + 1, z), safe, node, active, visited, queue);
        enqueueReachable(pack(x - 1, z), safe, node, active, visited, queue);
        enqueueReachable(pack(x, z + 1), safe, node, active, visited, queue);
        enqueueReachable(pack(x, z - 1), safe, node, active, visited, queue);
    }

    private static void enqueueReachable(long chunk, LongSet safe, LongSet node, LongSet active,
                                         LongSet visited, LongArrayFIFOQueue queue) {
        if (!visited.add(chunk)) return;
        if (hasAny(safe, node, active, chunk)) queue.enqueue(chunk);
    }

    private static void addActive(long chunk, LongSet safe, LongSet node, LongSet active, LongLinkedOpenHashSet added) {
        if (hasAny(safe, node, active, chunk)) return;
        if (active.add(chunk)) added.add(chunk);
    }

    private static boolean hasAny(LongSet safe, LongSet node, LongSet active, long chunk) {
        return safe.contains(chunk) || node.contains(chunk) || active.contains(chunk);
    }

    private static int minDistanceToSeeds(Set<Long> seeds, long chunk) {
        int chunkX = x(chunk);
        int chunkZ = z(chunk);
        double minDistance = Double.MAX_VALUE;
        for (long seed : seeds) {
            double dx = chunkX - x(seed);
            double dz = chunkZ - z(seed);
            minDistance = Math.min(minDistance, Math.sqrt(dx * dx + dz * dz));
        }
        return (int) Math.ceil(minDistance);
    }

    private static LongSet copyOf(Set<Long> chunks) {
        LongOpenHashSet result = new LongOpenHashSet(chunks.size());
        for (long chunk : chunks) result.add(chunk);
        return result;
    }

    private static List<Long> toList(LongLinkedOpenHashSet chunks) {
        List<Long> result = new ArrayList<>(chunks.size());
        LongIterator iterator = chunks.iterator();
        while (iterator.hasNext()) result.add(iterator.nextLong());
        return result;
    }

    public static long pack(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    public static int x(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    public static int z(long packed) {
        return (int) ((packed >> 32) & 0xFFFFFFFFL);
    }

    private record TargetStats(int nearestDistance, int furthestDistance, int largestComponent) {
    }
}
