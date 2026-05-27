package org.galaxy.beyond.api.system.zone.algorithm;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;

import java.util.*;

/**
 * 节点感知扩张器 —— 圆形扩张 + 连通性判定。
 * <p>
 * 从种子出发逐层外扩，每层通过 {@link #floodReachable} 判定节点组件是否可走通，
 * 够数即停。
 */
public class NodeAwareExpander {

    private final Function<Set<ChunkPos>, Set<Set<ChunkPos>>> componentGrouper;

    @FunctionalInterface
    public interface Function<T, R> { R apply(T t); }

    public NodeAwareExpander(Function<Set<ChunkPos>, Set<Set<ChunkPos>>> componentGrouper) {
        this.componentGrouper = componentGrouper;
    }

    /**
     * 逐层扩张直到通过本轮新拓张发现足够多的未完成节点组件。
     * <p>
     * 拓张前先做一次 flood fill 基线，排除掉当前已经可达的节点区块。
     * 只统计"本轮拓张之前不可达、本轮拓张之后变为可达"的新节点组件。
     *
     * @return 实际扩张到的半径，若达到 maxRadius 仍未找到足够新节点则返回 -1
     */
    public int expandUntilWrapped(ServerLevel level, Set<ChunkPos> seeds,
                                   Set<ChunkPos> uncompleted,
                                   int minConnections, int minRadius, int maxRadius,
                                   LevelZoneData lzd) {
        return expandUntilWrapped(level, seeds, uncompleted, minConnections, minRadius, maxRadius, lzd, lzd::addActive);
    }

    public int expandUntilWrapped(ServerLevel level, Set<ChunkPos> seeds,
                                   Set<ChunkPos> uncompleted,
                                   int minConnections, int minRadius, int maxRadius,
                                   LevelZoneData lzd,
                                   java.util.function.Predicate<ChunkPos> addActiveChunk) {
        // 拓张前基线：当前 zone 网络下已经可达的区块
        Set<ChunkPos> beforeReachable = floodReachable(seeds, lzd);

        // 过滤掉已可达的节点区块，只保留"等待本轮拓张发现"的
        Set<ChunkPos> newTargets = new HashSet<>(uncompleted);
        newTargets.removeAll(beforeReachable);

        Set<Set<ChunkPos>> targetComponents = componentGrouper.apply(newTargets);
        int needed = targetComponents.isEmpty() ? 0
                : Math.min(minConnections, targetComponents.size());

        int actualRadius = -1;

        for (int r = 1; r <= maxRadius; r++) {
            Set<ChunkPos> ring = ringOf(seeds, r);
            for (ChunkPos p : ring)
                if (!lzd.hasAny(p))
                    addActiveChunk.test(p);

            if (r >= minRadius) {
                if (needed == 0) {
                    // 无待包裹节点时继续扩张，确保覆盖邻近结构点位
                    int explorationR = Math.max(minRadius * 2, 40);
                    for (int rr = r + 1; rr <= explorationR && rr <= maxRadius; rr++) {
                        Set<ChunkPos> extra = ringOf(seeds, rr);
                        for (ChunkPos p : extra)
                            if (!lzd.hasAny(p))
                                addActiveChunk.test(p);
                    }
                    actualRadius = Math.min(explorationR, maxRadius);
                    break;
                }

                Set<ChunkPos> reachable = floodReachable(seeds, lzd);
                int reached = 0;
                for (var comp : targetComponents) {
                    for (ChunkPos c : comp)
                        if (reachable.contains(c)) { reached++; break; }
                }

                if (reached >= needed) {
                    // 计算已达组件中最远区块距 seeds 的距离，把圆推到完整包裹节点
                    int furthest = 0;
                    for (var comp : targetComponents) {
                        if (comp.stream().noneMatch(reachable::contains)) continue;
                        for (ChunkPos c : comp)
                            furthest = Math.max(furthest, minDistToSeeds(seeds, c));
                    }
                    actualRadius = Math.max(r, furthest);
                    if (actualRadius > maxRadius) actualRadius = maxRadius;
                    // 补填到 actualRadius
                    for (int rr = r + 1; rr <= actualRadius; rr++) {
                        Set<ChunkPos> extra = ringOf(seeds, rr);
                        for (ChunkPos p : extra)
                            if (!lzd.hasAny(p))
                                addActiveChunk.test(p);
                    }
                    break;
                }
            }
        }

        return actualRadius;
    }

    /**
     * 从 seeds 出发沿已 zone 区块 4 邻域 BFS，返回所有可走到的区块。
     */
    public static Set<ChunkPos> floodReachable(Set<ChunkPos> seeds, LevelZoneData lzd) {
        Set<ChunkPos> visited = new HashSet<>();
        Deque<ChunkPos> queue = new ArrayDeque<>();
        for (ChunkPos s : seeds) {
            if (lzd.hasAny(s)) { visited.add(s); queue.add(s); }
        }
        while (!queue.isEmpty()) {
            for (ChunkPos nb : ZoneHelper.neighbors4(queue.poll())) {
                if (!visited.add(nb)) continue;
                if (lzd.hasAny(nb)) queue.add(nb);
            }
        }
        return visited;
    }

    /** 圆的最外圈（半径 r 处的增量，不含内层） */
    public static Set<ChunkPos> ringOf(Set<ChunkPos> seeds, int r) {
        if (r == 0) return new HashSet<>(seeds);
        Set<ChunkPos> outer = expandCircleMulti(seeds, r);
        Set<ChunkPos> inner = expandCircleMulti(seeds, r - 1);
        outer.removeAll(inner);
        return outer;
    }

    private static Set<ChunkPos> expandCircleMulti(Set<ChunkPos> seeds, int r) {
        Set<ChunkPos> result = new HashSet<>();
        for (ChunkPos s : seeds) result.addAll(ZoneHelper.expandCircle(s, r));
        return result;
    }

    /** 区块 c 到 seeds 集合的最近欧几里得距离（向上取整） */
    private static int minDistToSeeds(Set<ChunkPos> seeds, ChunkPos c) {
        int cx = c.x, cz = c.z;
        double minDist = Double.MAX_VALUE;
        for (ChunkPos s : seeds) {
            double dx = cx - s.x, dz = cz - s.z;
            minDist = Math.min(minDist, Math.sqrt(dx * dx + dz * dz));
        }
        return (int) Math.ceil(minDist);
    }
}
