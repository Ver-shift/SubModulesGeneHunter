package org.galaxy.beyond.api.system.zone.algorithm;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 节点感知扩张器 —— 圆形扩张 + 连通性判定。
 * <p>
 * 只有通过已 zone 区块（含新填的 Active_Zone）能走到节点组件时，
 * 才算"可达"；有空地隔开的节点不算，需要继续扩张。
 */
public class NodeAwareExpander {

    private final CircularExpander expander = new CircularExpander();
    private final Function<Set<ChunkPos>, Set<Set<ChunkPos>>> componentGrouper;

    public NodeAwareExpander(Function<Set<ChunkPos>, Set<Set<ChunkPos>>> componentGrouper) {
        this.componentGrouper = componentGrouper;
    }

    public int expandUntilWrapped(ServerLevel level, Set<ChunkPos> seeds,
                                   Set<ChunkPos> uncompleted,
                                   int minConnections, int minRadius, int maxRadius,
                                   BiFunction<ChunkPos, ZoneType, Boolean> setZone,
                                   Function<ChunkPos, ZoneType> getZone) {
        Set<Set<ChunkPos>> targetComponents = componentGrouper.apply(uncompleted);
        int needed = targetComponents.isEmpty() ? 1
                : Math.min(minConnections, targetComponents.size());

        Set<Set<ChunkPos>> reachable = new HashSet<>();
        int actualRadius = maxRadius;

        for (int r = minRadius; r <= maxRadius; r++) {
            // 填充圆形 Active_Zone
            expander.expand(level, seeds, r, ZoneType.Active_Zone, setZone, getZone);

            // 连通性检测：通过所有 zone 区块（含刚填的 Active_Zone），
            // 从 seeds 出发能走到的节点组件才算"可达"
            Set<ChunkPos> reachableChunks = floodReachable(seeds, getZone);
            reachable.clear();
            for (var comp : targetComponents) {
                for (ChunkPos c : comp) {
                    if (reachableChunks.contains(c)) {
                        reachable.add(comp);
                        break;
                    }
                }
            }

            if (reachable.size() >= needed) {
                actualRadius = r;
                break;
            }
        }
        return actualRadius;
    }

    public boolean expandFixed(ServerLevel level, Set<ChunkPos> seeds, int radius,
                                BiFunction<ChunkPos, ZoneType, Boolean> setZone,
                                Function<ChunkPos, ZoneType> getZone) {
        return expander.expand(level, seeds, radius, ZoneType.Active_Zone, setZone, getZone);
    }

    /**
     * 从 seeds 出发，沿已 zone 的区块 4 邻域 flood fill，
     * 返回所有可达的区块（含 Node_Zone、Safe_Zone、Active_Zone）。
     */
    public static Set<ChunkPos> floodReachable(Set<ChunkPos> seeds,
                                                Function<ChunkPos, ZoneType> getZone) {
        Set<ChunkPos> visited = new HashSet<>();
        Deque<ChunkPos> queue = new ArrayDeque<>();
        for (ChunkPos s : seeds) {
            if (getZone.apply(s) != null) {
                visited.add(s);
                queue.add(s);
            }
        }
        while (!queue.isEmpty()) {
            for (ChunkPos nb : neighbors4(queue.poll())) {
                if (!visited.add(nb)) continue;
                if (getZone.apply(nb) != null) queue.add(nb);
            }
        }
        return visited;
    }

    private static List<ChunkPos> neighbors4(ChunkPos p) {
        int cx = toInt(p), cz = toIntZ(p);
        return List.of(
                new ChunkPos(cx + 1, cz), new ChunkPos(cx - 1, cz),
                new ChunkPos(cx, cz + 1), new ChunkPos(cx, cz - 1));
    }

    private static int toInt(ChunkPos p) { return p.getMinBlockX() >> 4; }
    private static int toIntZ(ChunkPos p) { return p.getMinBlockZ() >> 4; }
}
