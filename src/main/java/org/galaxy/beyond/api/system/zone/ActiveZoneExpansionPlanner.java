package org.galaxy.beyond.api.system.zone;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ActiveZoneExpansionPlanner {

    private ActiveZoneExpansionPlanner() {}

    public static Result plan(Collection<Chunk> currentNodeChunks,
                              Collection<Chunk> allNodeChunks,
                              Collection<Chunk> completedNodeChunks,
                              int minRadius,
                              int minUncompletedNodes,
                              int maxRadius) {
        if (currentNodeChunks == null || currentNodeChunks.isEmpty()) {
            throw new IllegalArgumentException("currentNodeChunks must not be empty");
        }

        int startRadius = Math.max(0, minRadius);
        int endRadius = Math.max(startRadius, maxRadius);
        Bounds currentBounds = Bounds.of(currentNodeChunks);
        Set<Chunk> allNodes = new HashSet<>(allNodeChunks);
        Set<Chunk> completed = new HashSet<>(completedNodeChunks);

        Result fallback = null;
        for (int radius = startRadius; radius <= endRadius; radius++) {
            Bounds bounds = currentBounds.expanded(radius);
            int count = countUncompletedNodeComponents(allNodes, completed, bounds);
            Result result = new Result(bounds, radius, count, count >= minUncompletedNodes);
            if (result.satisfied()) return result;
            fallback = result;
        }
        return fallback;
    }

    private static int countUncompletedNodeComponents(Set<Chunk> allNodes, Set<Chunk> completed, Bounds bounds) {
        Set<Chunk> remaining = new HashSet<>();
        for (Chunk chunk : allNodes) {
            if (!completed.contains(chunk) && bounds.contains(chunk)) {
                remaining.add(chunk);
            }
        }

        int components = 0;
        while (!remaining.isEmpty()) {
            components++;
            floodFill(remaining, remaining.iterator().next());
        }
        return components;
    }

    private static void floodFill(Set<Chunk> remaining, Chunk seed) {
        ArrayDeque<Chunk> queue = new ArrayDeque<>();
        queue.add(seed);
        remaining.remove(seed);
        while (!queue.isEmpty()) {
            Chunk chunk = queue.removeFirst();
            for (Chunk neighbor : chunk.neighbors4()) {
                if (remaining.remove(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
    }

    public record Result(Bounds bounds, int radius, int uncompletedNodeCount, boolean satisfied) {
    }

    public record Bounds(int minX, int minZ, int maxX, int maxZ) {
        public static Bounds of(Collection<Chunk> chunks) {
            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (Chunk chunk : chunks) {
                minX = Math.min(minX, chunk.x());
                minZ = Math.min(minZ, chunk.z());
                maxX = Math.max(maxX, chunk.x());
                maxZ = Math.max(maxZ, chunk.z());
            }
            return new Bounds(minX, minZ, maxX, maxZ);
        }

        public Bounds expanded(int radius) {
            return new Bounds(minX - radius, minZ - radius, maxX + radius, maxZ + radius);
        }

        public boolean contains(Chunk chunk) {
            return chunk.x() >= minX && chunk.x() <= maxX && chunk.z() >= minZ && chunk.z() <= maxZ;
        }
    }

    public record Chunk(int x, int z) {
        Set<Chunk> neighbors4() {
            return Set.of(
                    new Chunk(x + 1, z),
                    new Chunk(x - 1, z),
                    new Chunk(x, z + 1),
                    new Chunk(x, z - 1)
            );
        }
    }
}
