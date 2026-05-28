package org.galaxy.beyond.api.system.zone.async;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PackedZoneExpander {

    public ZoneExpansionResult expand(ZoneExpansionRequest request) {
        Set<Long> safe = Set.copyOf(request.safe());
        Set<Long> node = Set.copyOf(request.node());
        Set<Long> active = new HashSet<>(request.active());
        Set<Long> added = new LinkedHashSet<>();

        Set<Long> beforeReachable = floodReachable(request.seeds(), safe, node, active);
        Set<Long> newTargets = new HashSet<>(request.uncompleted());
        newTargets.removeAll(beforeReachable);

        List<Set<Long>> targetComponents = connectedComponents(newTargets);
        int needed = targetComponents.isEmpty() ? 0 : Math.min(request.minConnections(), targetComponents.size());
        int actualRadius = -1;

        for (int radius = 1; radius <= request.maxRadius(); radius++) {
            for (long chunk : ringOf(request.seeds(), radius)) {
                if (hasAny(safe, node, active, chunk)) continue;
                if (active.add(chunk)) added.add(chunk);
            }

            if (radius < request.minRadius()) continue;
            if (needed == 0) {
                int explorationRadius = Math.min(Math.max(request.minRadius() * 2, 40), request.maxRadius());
                for (int extraRadius = radius + 1; extraRadius <= explorationRadius; extraRadius++) {
                    for (long chunk : ringOf(request.seeds(), extraRadius)) {
                        if (hasAny(safe, node, active, chunk)) continue;
                        if (active.add(chunk)) added.add(chunk);
                    }
                }
                actualRadius = explorationRadius;
                break;
            }

            Set<Long> reachable = floodReachable(request.seeds(), safe, node, active);
            int reached = countReachedComponents(targetComponents, reachable);
            if (reached < needed) continue;

            int furthest = furthestReachedDistance(request.seeds(), targetComponents, reachable);
            actualRadius = Math.min(Math.max(radius, furthest), request.maxRadius());
            for (int extraRadius = radius + 1; extraRadius <= actualRadius; extraRadius++) {
                for (long chunk : ringOf(request.seeds(), extraRadius)) {
                    if (hasAny(safe, node, active, chunk)) continue;
                    if (active.add(chunk)) added.add(chunk);
                }
            }
            break;
        }

        return new ZoneExpansionResult(request.jobId(), List.copyOf(added), actualRadius, actualRadius >= 0);
    }

    private static int countReachedComponents(List<Set<Long>> components, Set<Long> reachable) {
        int reached = 0;
        for (Set<Long> component : components) {
            for (long chunk : component) {
                if (reachable.contains(chunk)) {
                    reached++;
                    break;
                }
            }
        }
        return reached;
    }

    private static int furthestReachedDistance(Set<Long> seeds, List<Set<Long>> components, Set<Long> reachable) {
        int furthest = 0;
        for (Set<Long> component : components) {
            boolean reached = false;
            for (long chunk : component) {
                if (reachable.contains(chunk)) {
                    reached = true;
                    break;
                }
            }
            if (!reached) continue;
            for (long chunk : component) {
                furthest = Math.max(furthest, minDistanceToSeeds(seeds, chunk));
            }
        }
        return furthest;
    }

    private static Set<Long> floodReachable(Set<Long> seeds, Set<Long> safe, Set<Long> node, Set<Long> active) {
        Set<Long> visited = new HashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        for (long seed : seeds) {
            if (!hasAny(safe, node, active, seed)) continue;
            visited.add(seed);
            queue.add(seed);
        }
        while (!queue.isEmpty()) {
            for (long neighbor : neighbors4(queue.poll())) {
                if (!visited.add(neighbor)) continue;
                if (hasAny(safe, node, active, neighbor)) queue.add(neighbor);
            }
        }
        return visited;
    }

    private static List<Set<Long>> connectedComponents(Set<Long> chunks) {
        Set<Long> remaining = new HashSet<>(chunks);
        List<Set<Long>> components = new ArrayList<>();
        while (!remaining.isEmpty()) {
            long seed = remaining.iterator().next();
            Set<Long> component = new HashSet<>();
            ArrayDeque<Long> queue = new ArrayDeque<>();
            remaining.remove(seed);
            queue.add(seed);
            while (!queue.isEmpty()) {
                long chunk = queue.poll();
                component.add(chunk);
                for (long neighbor : neighbors8(chunk)) {
                    if (remaining.remove(neighbor)) queue.add(neighbor);
                }
            }
            components.add(component);
        }
        return components;
    }

    private static Set<Long> ringOf(Set<Long> seeds, int radius) {
        if (radius == 0) return new HashSet<>(seeds);
        Set<Long> outer = expandCircleMulti(seeds, radius);
        outer.removeAll(expandCircleMulti(seeds, radius - 1));
        return outer;
    }

    private static Set<Long> expandCircleMulti(Set<Long> seeds, int radius) {
        Set<Long> result = new LinkedHashSet<>();
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

    private static List<Long> neighbors4(long chunk) {
        int x = x(chunk);
        int z = z(chunk);
        return List.of(
                pack(x + 1, z),
                pack(x - 1, z),
                pack(x, z + 1),
                pack(x, z - 1)
        );
    }

    private static List<Long> neighbors8(long chunk) {
        int x = x(chunk);
        int z = z(chunk);
        return List.of(
                pack(x + 1, z),
                pack(x - 1, z),
                pack(x, z + 1),
                pack(x, z - 1),
                pack(x + 1, z + 1),
                pack(x - 1, z - 1),
                pack(x + 1, z - 1),
                pack(x - 1, z + 1)
        );
    }

    private static boolean hasAny(Set<Long> safe, Set<Long> node, Set<Long> active, long chunk) {
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

    public static long pack(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    public static int x(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    public static int z(long packed) {
        return (int) ((packed >> 32) & 0xFFFFFFFFL);
    }
}
