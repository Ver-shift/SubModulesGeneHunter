package org.galaxy.beyond.api.system.zone.util;

import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ZoneGeometry {

    private ZoneGeometry() {
    }

    public static Set<ChunkPos> expandCircle(ChunkPos center, int radius) {
        Set<ChunkPos> result = new LinkedHashSet<>();
        long radiusSqr = (long) radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            long dxSqr = (long) dx * dx;
            int dzMax = (int) Math.sqrt(radiusSqr - dxSqr);
            for (int dz = -dzMax; dz <= dzMax; dz++) {
                result.add(new ChunkPos(center.x() + dx, center.z() + dz));
            }
        }
        return result;
    }

    public static Set<ChunkPos> expandSquare(ChunkPos center, int radius) {
        Set<ChunkPos> result = new LinkedHashSet<>((2 * radius + 1) * (2 * radius + 1));
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                result.add(new ChunkPos(center.x() + dx, center.z() + dz));
            }
        }
        return result;
    }

    public static Bounds boundsOf(Collection<ChunkPos> chunks) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (ChunkPos chunk : chunks) {
            if (chunk.x() < minX) minX = chunk.x();
            if (chunk.x() > maxX) maxX = chunk.x();
            if (chunk.z() < minZ) minZ = chunk.z();
            if (chunk.z() > maxZ) maxZ = chunk.z();
        }
        return new Bounds(minX, minZ, maxX, maxZ);
    }

    public record Bounds(int minX, int minZ, int maxX, int maxZ) {
        public Bounds expanded(int radius) {
            return new Bounds(minX - radius, minZ - radius, maxX + radius, maxZ + radius);
        }

        public boolean contains(ChunkPos pos) {
            return pos.x() >= minX && pos.x() <= maxX && pos.z() >= minZ && pos.z() <= maxZ;
        }

        public Set<ChunkPos> allChunks() {
            Set<ChunkPos> chunks = new LinkedHashSet<>();
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    chunks.add(new ChunkPos(x, z));
                }
            }
            return chunks;
        }
    }
}
