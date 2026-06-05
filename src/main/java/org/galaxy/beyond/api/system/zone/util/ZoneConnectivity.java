package org.galaxy.beyond.api.system.zone.util;

import net.minecraft.world.level.ChunkPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ZoneConnectivity {

    private ZoneConnectivity() {
    }

    public static List<Set<ChunkPos>> findConnectedComponents(Collection<ChunkPos> chunks) {
        Set<ChunkPos> remaining = new HashSet<>(chunks);
        List<Set<ChunkPos>> components = new ArrayList<>();
        while (!remaining.isEmpty()) {
            ChunkPos seed = remaining.iterator().next();
            Set<ChunkPos> component = new HashSet<>();
            Deque<ChunkPos> queue = new ArrayDeque<>();
            queue.add(seed);
            remaining.remove(seed);
            while (!queue.isEmpty()) {
                ChunkPos pos = queue.poll();
                component.add(pos);
                for (ChunkPos neighbor : neighbors4(pos)) {
                    if (remaining.remove(neighbor)) queue.add(neighbor);
                }
            }
            components.add(component);
        }
        return components;
    }

    public static Set<ChunkPos> neighbors4(ChunkPos pos) {
        return Set.of(
                new ChunkPos(pos.x() + 1, pos.z()),
                new ChunkPos(pos.x() - 1, pos.z()),
                new ChunkPos(pos.x(), pos.z() + 1),
                new ChunkPos(pos.x(), pos.z() - 1)
        );
    }
}
