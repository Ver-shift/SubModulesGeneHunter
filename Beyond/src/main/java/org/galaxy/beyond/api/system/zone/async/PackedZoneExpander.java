package org.galaxy.beyond.api.system.zone.async;

import org.galaxy.beyond.api.system.zone.util.PackedChunkPos;

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
        Set<Long> active = Set.copyOf(request.active());

        Set<Long> beforeReachable = floodReachable(request.seeds(), safe, node, active);
        Set<Long> newTargets = new HashSet<>(request.uncompleted());
        newTargets.removeAll(beforeReachable);

        List<Set<Long>> targetComponents = connectedComponents(newTargets);
        int needed = targetComponents.isEmpty() ? 0 : Math.min(request.minConnections(), targetComponents.size());
        Set<Set<Long>> selected = new LinkedHashSet<>();

        org.galaxy.beyond.Beyond.debugInfo(
                "[Zone][EXPAND] jobId={}, seeds={}, safe={}, node={}, active={}, uncompleted={}, beforeReachable={}, targetComponents={}, needed={}, minRadius={}, maxRadius={}",
                request.jobId(),
                request.seeds().size(),
                request.safe().size(),
                request.node().size(),
                request.active().size(),
                request.uncompleted().size(),
                beforeReachable.size(),
                targetComponents.size(),
                needed,
                request.minRadius(),
                request.maxRadius()
        );

        int scanRadius = scanTargets(request.seeds(), targetComponents, selected, request.minRadius(), request.maxRadius(), needed);
        boolean success = needed == 0 || selected.size() >= needed;
        int actualRadius = success ? Math.max(request.minRadius(), scanRadius) : -1;
        if (success) actualRadius = expandRadiusToCoverSelected(request.seeds(), targetComponents, selected, actualRadius);

        Set<Long> finalArea = success ? expandCircleMulti(request.seeds(), actualRadius) : Set.of();
        if (success) includeTouchedTargets(request.seeds(), targetComponents, selected, finalArea);
        if (success) actualRadius = expandRadiusToCoverSelected(request.seeds(), targetComponents, selected, actualRadius);

        Set<Long> added = new LinkedHashSet<>();
        if (success) {
            for (long chunk : expandCircleMulti(request.seeds(), actualRadius)) {
                if (hasAny(safe, node, active, chunk)) continue;
                added.add(chunk);
            }
        }

        org.galaxy.beyond.Beyond.debugInfo(
                "[Zone][EXPAND_DONE] jobId={}, added={}, radius={}, reachedNodeZones={}, success={}",
                request.jobId(),
                added.size(),
                actualRadius,
                selected.size(),
                success
        );

        return new ZoneExpansionResult(request.jobId(), List.copyOf(added), actualRadius, selected.size(), success);
    }

    private static int scanTargets(Set<Long> seeds, List<Set<Long>> targets, Set<Set<Long>> selected,
                                   int minRadius, int maxRadius, int needed) {
        int radius = 0;
        for (; radius <= maxRadius; radius++) {
            includeTargetsWithin(seeds, targets, selected, radius);
            if (radius >= minRadius && selected.size() >= needed) break;
        }
        return radius > maxRadius ? maxRadius : radius;
    }

    private static void includeTargetsWithin(Set<Long> seeds, List<Set<Long>> targets, Set<Set<Long>> selected, int radius) {
        for (Set<Long> component : targets) {
            if (selected.contains(component)) continue;
            for (long chunk : component) {
                if (minDistanceToSeeds(seeds, chunk) <= radius) {
                    selected.add(component);
                    break;
                }
            }
        }
    }

    private static void includeTouchedTargets(Set<Long> seeds, List<Set<Long>> targets, Set<Set<Long>> selected, Set<Long> area) {
        int previousSize;
        do {
            previousSize = selected.size();
            for (Set<Long> component : targets) {
                if (selected.contains(component)) continue;
                if (touches(component, area)) selected.add(component);
            }
            int radius = expandRadiusToCoverSelected(seeds, targets, selected, 0);
            area.clear();
            area.addAll(expandCircleMulti(seeds, radius));
        } while (selected.size() != previousSize);
    }

    private static boolean touches(Set<Long> component, Set<Long> area) {
        for (long chunk : component) {
            if (area.contains(chunk)) return true;
        }
        return false;
    }

    private static int expandRadiusToCoverSelected(Set<Long> seeds, List<Set<Long>> targets,
                                                   Set<Set<Long>> selected, int minRadius) {
        int radius = minRadius;
        for (Set<Long> component : targets) {
            if (!selected.contains(component)) continue;
            for (long chunk : component) {
                radius = Math.max(radius, minDistanceToSeeds(seeds, chunk));
            }
        }
        return radius;
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

    private static Set<Long> expandCircleMulti(Set<Long> seeds, int radius) {
        Set<Long> result = new LinkedHashSet<>();
        long radiusSqr = (long) radius * radius;
        for (long seed : seeds) {
            int centerX = PackedChunkPos.x(seed);
            int centerZ = PackedChunkPos.z(seed);
            for (int dx = -radius; dx <= radius; dx++) {
                long dxSqr = (long) dx * dx;
                int dzMax = (int) Math.sqrt(radiusSqr - dxSqr);
                for (int dz = -dzMax; dz <= dzMax; dz++) {
                    result.add(PackedChunkPos.pack(centerX + dx, centerZ + dz));
                }
            }
        }
        return result;
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

    private static List<Long> neighbors8(long chunk) {
        int x = PackedChunkPos.x(chunk);
        int z = PackedChunkPos.z(chunk);
        return List.of(
                PackedChunkPos.pack(x + 1, z),
                PackedChunkPos.pack(x - 1, z),
                PackedChunkPos.pack(x, z + 1),
                PackedChunkPos.pack(x, z - 1),
                PackedChunkPos.pack(x + 1, z + 1),
                PackedChunkPos.pack(x - 1, z - 1),
                PackedChunkPos.pack(x + 1, z - 1),
                PackedChunkPos.pack(x - 1, z + 1)
        );
    }
    private static boolean hasAny(Set<Long> safe, Set<Long> node, Set<Long> active, long chunk) {
        return safe.contains(chunk) || node.contains(chunk) || active.contains(chunk);
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
}
