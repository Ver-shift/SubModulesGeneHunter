package org.galaxy.beyond.api.system.zone;

import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class ZoneHelper {

    // ---- 方形扩展 ----

    public static Set<ChunkPos> expandCircle(ChunkPos center, int radius) {
        Set<ChunkPos> r = new LinkedHashSet<>();
        long r2 = (long) radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            long dx2 = (long) dx * dx;
            int dzMax = (int) Math.sqrt(r2 - dx2);
            for (int dz = -dzMax; dz <= dzMax; dz++)
                r.add(new ChunkPos(center.x() + dx, center.z() + dz));
        }
        return r;
    }

    public static Set<ChunkPos> expandSquare(ChunkPos center, int radius) {
        Set<ChunkPos> result = new LinkedHashSet<>((2 * radius + 1) * (2 * radius + 1));
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++)
                result.add(new ChunkPos(center.x() + dx, center.z() + dz));
        return result;
    }

    // ---- 包围盒 ----

    public record Bounds(int minX, int minZ, int maxX, int maxZ) {
        public Bounds expanded(int r) { return new Bounds(minX - r, minZ - r, maxX + r, maxZ + r); }
        public boolean contains(ChunkPos p) { return p.x() >= minX && p.x() <= maxX && p.z() >= minZ && p.z() <= maxZ; }
        public Set<ChunkPos> allChunks() {
            Set<ChunkPos> s = new LinkedHashSet<>();
            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++)
                    s.add(new ChunkPos(x, z));
            return s;
        }
    }

    public static Bounds boundsOf(Collection<ChunkPos> chunks) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (ChunkPos p : chunks) {
            if (p.x() < minX) minX = p.x(); if (p.x() > maxX) maxX = p.x();
            if (p.z() < minZ) minZ = p.z(); if (p.z() > maxZ) maxZ = p.z();
        }
        return new Bounds(minX, minZ, maxX, maxZ);
    }

    // ---- 类型过滤（用于渲染器） ----

    public static FilteredChunks filterByType(Set<Map.Entry<ChunkPos, ZoneType>> entries, ZoneType type) {
        Set<ChunkPos> chunks = new HashSet<>();
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (var e : entries) {
            if (e.getValue() != type) continue;
            ChunkPos p = e.getKey();
            chunks.add(p);
            if (p.x() < minX) minX = p.x(); if (p.x() > maxX) maxX = p.x();
            if (p.z() < minZ) minZ = p.z(); if (p.z() > maxZ) maxZ = p.z();
        }
        return new FilteredChunks(chunks, new Bounds(minX, minZ, maxX, maxZ));
    }

    public record FilteredChunks(Set<ChunkPos> chunks, Bounds bounds) {
        public boolean isEmpty() { return chunks.isEmpty(); }
    }

    // ---- 连通分量 ----

    public static List<Set<ChunkPos>> findConnectedComponents(Collection<ChunkPos> chunks) {
        Set<ChunkPos> remaining = new HashSet<>(chunks);
        List<Set<ChunkPos>> components = new ArrayList<>();
        while (!remaining.isEmpty()) {
            ChunkPos seed = remaining.iterator().next();
            Set<ChunkPos> component = new HashSet<>();
            Deque<ChunkPos> queue = new ArrayDeque<>();
            queue.add(seed); remaining.remove(seed);
            while (!queue.isEmpty()) {
                ChunkPos p = queue.poll();
                component.add(p);
                for (ChunkPos n : neighbors4(p))
                    if (remaining.remove(n)) queue.add(n);
            }
            components.add(component);
        }
        return components;
    }

    public static Set<ChunkPos> neighbors4(ChunkPos p) {
        return Set.of(
                new ChunkPos(p.x() + 1, p.z()), new ChunkPos(p.x() - 1, p.z()),
                new ChunkPos(p.x(), p.z() + 1), new ChunkPos(p.x(), p.z() - 1));
    }
}
