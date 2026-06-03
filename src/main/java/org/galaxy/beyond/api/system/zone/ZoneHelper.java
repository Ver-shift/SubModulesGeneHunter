package org.galaxy.beyond.api.system.zone;

import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.zone.util.ZoneConnectivity;
import org.galaxy.beyond.api.system.zone.util.ZoneGeometry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ZoneHelper {

    public static Set<ChunkPos> expandCircle(ChunkPos center, int radius) {
        return ZoneGeometry.expandCircle(center, radius);
    }

    public static Set<ChunkPos> expandSquare(ChunkPos center, int radius) {
        return ZoneGeometry.expandSquare(center, radius);
    }

    public static Bounds boundsOf(Collection<ChunkPos> chunks) {
        return Bounds.from(ZoneGeometry.boundsOf(chunks));
    }

    public static FilteredChunks filterByType(Set<Map.Entry<ChunkPos, ZoneType>> entries, ZoneType type) {
        Set<ChunkPos> chunks = new HashSet<>();
        for (var entry : entries) {
            if (entry.getValue() == type) chunks.add(entry.getKey());
        }
        return new FilteredChunks(chunks, boundsOf(chunks));
    }

    public static List<Set<ChunkPos>> findConnectedComponents(Collection<ChunkPos> chunks) {
        return ZoneConnectivity.findConnectedComponents(chunks);
    }

    public static Set<ChunkPos> neighbors4(ChunkPos pos) {
        return ZoneConnectivity.neighbors4(pos);
    }

    public record Bounds(int minX, int minZ, int maxX, int maxZ) {
        private static Bounds from(ZoneGeometry.Bounds bounds) {
            return new Bounds(bounds.minX(), bounds.minZ(), bounds.maxX(), bounds.maxZ());
        }

        public Bounds expanded(int radius) {
            return from(new ZoneGeometry.Bounds(minX, minZ, maxX, maxZ).expanded(radius));
        }

        public boolean contains(ChunkPos pos) {
            return new ZoneGeometry.Bounds(minX, minZ, maxX, maxZ).contains(pos);
        }

        public Set<ChunkPos> allChunks() {
            return new ZoneGeometry.Bounds(minX, minZ, maxX, maxZ).allChunks();
        }
    }

    public record FilteredChunks(Set<ChunkPos> chunks, Bounds bounds) {
        public boolean isEmpty() {
            return chunks.isEmpty();
        }
    }
}
