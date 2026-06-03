package org.galaxy.beyond.api.system.zone.async;

import org.galaxy.beyond.api.system.zone.util.PackedChunkPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 纯区块级可活动区域拓展算法。
 * <p>
 * 核心规则：
 * <ol>
 *     <li>先把未完成节点按拓展前 active 状态分成已发现节点和新节点。</li>
 *     <li>只有新节点会满足 {@code minConnections}，已发现节点只参与统计播报。</li>
 *     <li>如果在 {@code maxRadius} 内找到了足够新节点，只拓展到能覆盖这些新节点的半径。</li>
 *     <li>如果在 {@code maxRadius} 内没有新节点，或者新节点数量不够，仍然拓展到 {@code maxRadius}。</li>
 * </ol>
 * 回退到 {@code maxRadius} 是故意的。可活动区域本身就是搜索空间；没有足够新节点时只返回
 * {@code minRadius} 会让探索停滞，玩家会感觉系统没有继续向外搜索。
 * <p>
 * 性能规则：
 * 不要在这里对全部 active 区块做 flood-fill。active 区块可能已经有几十万个。
 * 这个类只处理服务端线程传入的快照，执行一次有边界的节点选择，再生成一次有边界的圆形拓展。
 */
public class PackedZoneExpander {

    /**
     * 计算本次需要新增的 active 区块列表。
     * <p>
     * 步骤：
     * <ol>
     *     <li>把 safe、node、active 合成一个 blocked 查询表，避免重复加入已有区块。</li>
     *     <li>从未完成节点里移除 safe 和 active 覆盖的异常区块，但保留 Node_Zone 目标。</li>
     *     <li>把剩余节点区块按连通关系合并为节点区域，并分成已发现节点和新节点。</li>
     *     <li>从最近的新节点区域开始选择，直到满足 {@code minConnections} 或超过 {@code maxRadius}。</li>
     *     <li>决定最终半径：新节点足够时覆盖选中新节点；新节点不足或没有新节点时使用 {@code maxRadius}。</li>
     *     <li>从所有种子区块生成圆形拓展，只返回 blocked 中不存在的新区块。</li>
     * </ol>
     */
    public ZoneExpansionResult expand(ZoneExpansionRequest request) {
        Set<Long> blocked = new HashSet<>(request.safe().size() + request.node().size() + request.active().size());
        blocked.addAll(request.safe());
        blocked.addAll(request.node());
        blocked.addAll(request.active());

        // Node_Zone 本身就是要连接的目标，不能因为它已经注册到区域数据里就移除。
        Set<Long> newTargets = new HashSet<>(request.uncompleted());
        newTargets.removeAll(request.safe());
        newTargets.removeAll(request.active());

        List<Set<Long>> targetComponents = connectedComponents(newTargets);
        List<Set<Long>> discoveredTargets = new ArrayList<>();
        List<Set<Long>> newTargetComponents = new ArrayList<>();
        for (Set<Long> component : targetComponents) {
            if (touchesActive(component, request.active())) discoveredTargets.add(component);
            else newTargetComponents.add(component);
        }

        int needed = newTargetComponents.isEmpty() ? 0 : Math.min(request.minConnections(), newTargetComponents.size());
        Set<Set<Long>> selectedNewTargets = new LinkedHashSet<>();

        int scanRadius = newTargetComponents.isEmpty()
                ? request.maxRadius()
                : selectNearestTargets(request.seeds(), newTargetComponents, selectedNewTargets, request.minRadius(), request.maxRadius(), needed);
        Set<Set<Long>> coveredTargets = new LinkedHashSet<>(selectedNewTargets);
        boolean foundEnoughTargets = needed == 0 || selectedNewTargets.size() >= needed;
        int actualRadius = Math.max(request.minRadius(), scanRadius);
        if (foundEnoughTargets)
            actualRadius = expandRadiusToCoverSelected(request.seeds(), targetComponents, coveredTargets, actualRadius);
        else
            actualRadius = request.maxRadius();
        if (request.type().scanExpandedAreaTargets()) {
            actualRadius = scanExpandedAreaTargets(request.seeds(), targetComponents, targetComponents, coveredTargets, actualRadius);
        }

        Set<Long> added = new LinkedHashSet<>();
        for (long chunk : expandCircleMulti(request.seeds(), actualRadius)) {
            if (blocked.contains(chunk)) continue;
            added.add(chunk);
        }

        return new ZoneExpansionResult(request.jobId(), List.copyOf(added), actualRadius,
                countTargets(coveredTargets, discoveredTargets), countTargets(coveredTargets, newTargetComponents), true);
    }

    /**
     * 按距离选择最近的节点区域。
     * <p>
     * {@code minRadius} 只保证基础拓展下限；真正决定搜索是否停止的是节点数量和 {@code maxRadius}。
     */
    private static int selectNearestTargets(Set<Long> seeds, List<Set<Long>> targets, Set<Set<Long>> selected,
                                            int minRadius, int maxRadius, int needed) {
        int radius = minRadius;
        while (selected.size() < needed) {
            Set<Long> nearest = nearestTarget(seeds, targets, selected);
            if (nearest == null) break;
            int distance = distanceToSeeds(seeds, nearest);
            if (distance > maxRadius) break;
            selected.add(nearest);
            radius = Math.max(radius, distance);
        }
        return radius;
    }

    /**
     * 查找还没有被选中的最近节点区域。
     * <p>
     * 这里故意按节点区域选择，而不是一圈一圈按半径扫描。半径步进扫描会反复计算所有区块距离，
     * 节点区块很多时会明显变慢。
     */
    private static Set<Long> nearestTarget(Set<Long> seeds, List<Set<Long>> targets, Set<Set<Long>> selected) {
        Set<Long> nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (Set<Long> component : targets) {
            if (selected.contains(component)) continue;
            int distance = distanceToSeeds(seeds, component);
            if (distance < nearestDistance) {
                nearest = component;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    /**
     * 返回某个节点区域到所有种子区块的最小区块距离。
     * <p>
     * 这个距离同时用于决定节点优先级，以及覆盖该节点区域需要的半径。
     */
    private static int distanceToSeeds(Set<Long> seeds, Set<Long> chunks) {
        int distance = Integer.MAX_VALUE;
        for (long chunk : chunks) {
            distance = Math.min(distance, minDistanceToSeeds(seeds, chunk));
        }
        return distance;
    }

    /**
     * 保证最终半径能完整覆盖所有已选中的节点区域。
     * <p>
     * 选择节点区域时可能只因为其中一个区块离种子近。这里会再次检查整个节点区域，
     * 把半径扩大到能包含该区域里的所有区块。
     */
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

    /**
     * 补扫最终打开半径内实际碰到的节点区域。
     * <p>
     * 选择阶段只保证满足最低连接数；最终拓展圆可能还覆盖其他节点。如果不补扫这些节点，
     * 结果统计会偏少，后续区域提示也容易看起来像扫描不完整。
     */
    private static int scanExpandedAreaTargets(Set<Long> seeds, List<Set<Long>> allTargets,
                                               List<Set<Long>> newTargets, Set<Set<Long>> selected, int radius) {
        int currentRadius = radius;
        boolean changed;
        do {
            changed = false;
            for (Set<Long> component : newTargets) {
                if (selected.contains(component) || distanceToSeeds(seeds, component) > currentRadius) continue;
                selected.add(component);
                changed = true;
            }
            if (changed) currentRadius = expandRadiusToCoverSelected(seeds, allTargets, selected, currentRadius);
        } while (changed);
        return currentRadius;
    }

    private static int countTargets(Set<Set<Long>> selected, List<Set<Long>> targets) {
        int count = 0;
        for (Set<Long> target : targets) {
            if (selected.contains(target)) count++;
        }
        return count;
    }

    /**
     * 使用 8 方向邻接把节点区块合并成逻辑节点区域。
     * <p>
     * 对角线相邻也算同一个节点区域，因为结构范围通常是矩形或不规则形状，
     * 对角接触时也应该视为同一个被发现的节点区域。
     */
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

    /**
     * 生成所有种子区块周围圆形范围内的区块。
     * <p>
     * 这是唯一真正创建大范围探索空间的地方。必须受最终半径限制；
     * 不要把它放进循环里反复增长，否则节点密集时区块数量会膨胀得很快。
     */
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

    private static boolean touchesActive(Set<Long> chunks, Set<Long> active) {
        for (long chunk : chunks) {
            if (active.contains(chunk)) return true;
            for (long neighbor : neighbors4(chunk)) {
                if (active.contains(neighbor)) return true;
            }
        }
        return false;
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
