package org.galaxy.beyond.api.system.zone;

import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class ZoneHelper {

    // ============================================================
    // Sobel 边缘检测
    // ============================================================

    /**
     * 使用 Sobel 算子对稀疏 zone 数据进行边缘检测。
     * <p>
     * 将 Map&#60;ChunkPos, ZoneType&#62; 转为以 byte mask 为像素值的稠密网格，
     * 分别用水平/垂直 Sobel 核卷积，组合梯度幅值后按阈值筛选边缘区块。
     *
     * @param zones     稀疏 zone 数据
     * @param threshold 梯度阈值（0~255），越高边缘越少
     * @return 被判定为边缘的 ChunkPos 集合
     */
    public static Set<ChunkPos> sobelEdgeDetect(Map<ChunkPos, ZoneType> zones, int threshold) {
        if (zones.isEmpty()) return Collections.emptySet();

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (ChunkPos p : zones.keySet()) {
            if (p.x() < minX) minX = p.x();
            if (p.x() > maxX) maxX = p.x();
            if (p.z() < minZ) minZ = p.z();
            if (p.z() > maxZ) maxZ = p.z();
        }

        int w = maxX - minX + 1;
        int h = maxZ - minZ + 1;
        var grid = new int[h][w];
        for (var e : zones.entrySet()) {
            grid[e.getKey().z() - minZ][e.getKey().x() - minX] = e.getValue().mask() & 0xFF;
        }

        // Sobel kernels
        // Gx: [[-1,0,1],[-2,0,2],[-1,0,1]]   Gy: [[-1,-2,-1],[0,0,0],[1,2,1]]
        Set<ChunkPos> edges = new LinkedHashSet<>();
        for (int z = 1; z < h - 1; z++) {
            for (int x = 1; x < w - 1; x++) {
                int gx = -grid[z - 1][x - 1] - 2 * grid[z][x - 1] - grid[z + 1][x - 1]
                         + grid[z - 1][x + 1] + 2 * grid[z][x + 1] + grid[z + 1][x + 1];
                int gy = -grid[z - 1][x - 1] - 2 * grid[z - 1][x] - grid[z - 1][x + 1]
                         + grid[z + 1][x - 1] + 2 * grid[z + 1][x] + grid[z + 1][x + 1];
                int mag = (int) Math.sqrt(gx * gx + gy * gy);
                if (mag >= threshold) {
                    edges.add(new ChunkPos(x + minX, z + minZ));
                }
            }
        }
        return edges;
    }

    // ============================================================
    // 方形扩展
    // ============================================================

    /**
     * 以 center 为中心，返回半径为 radius 的方形区域内的所有 ChunkPos。
     *
     * @param center 中心区块坐标
     * @param radius 半径（0 表示仅中心自身）
     * @return 方形内所有 ChunkPos 集合
     */
    public static Set<ChunkPos> expandSquare(ChunkPos center, int radius) {
        if (radius < 0) throw new IllegalArgumentException("radius must be >= 0");
        Set<ChunkPos> result = new LinkedHashSet<>((2 * radius + 1) * (2 * radius + 1));
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                result.add(new ChunkPos(center.x() + dx, center.z() + dz));
            }
        }
        return result;
    }

    // ============================================================
    // 圆形扩展（欧几里得距离）
    // ============================================================

    /**
     * 以 center 为中心，返回半径为 radius 的圆形区域内的所有 ChunkPos。
     * 使用欧几里得距离：dx² + dz² &#60;= radius²。
     *
     * @param center 中心区块坐标
     * @param radius 半径（区块数）
     * @return 圆形内所有 ChunkPos 集合
     */
    public static Set<ChunkPos> expandCircle(ChunkPos center, int radius) {
        if (radius < 0) throw new IllegalArgumentException("radius must be >= 0");
        Set<ChunkPos> result = new LinkedHashSet<>();
        long r2 = (long) radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            long dx2 = (long) dx * dx;
            int dzMax = (int) Math.sqrt(r2 - dx2);
            for (int dz = -dzMax; dz <= dzMax; dz++) {
                result.add(new ChunkPos(center.x() + dx, center.z() + dz));
            }
        }
        return result;
    }

    // ============================================================
    // 轮廓添加（形态学膨胀）
    // ============================================================

    /**
     * 在已有 shape 的最外圈添加 n 层轮廓区块（形态学膨胀）。
     * 返回的是新增的轮廓区块，不包含原 shape。
     *
     * @param shape 已有形状的区块集合
     * @param n     向外扩展的轮廓层数
     * @return 新增轮廓区块（与 shape 不相交）
     */
    public static Set<ChunkPos> addOutline(Set<ChunkPos> shape, int n) {
        if (n <= 0 || shape.isEmpty()) return Collections.emptySet();
        Set<ChunkPos> dilated = dilate(shape, n);
        dilated.removeAll(shape);
        return dilated;
    }

    // 内部：对 shape 进行 k 次膨胀（4邻域），返回膨胀后的完整集合（含原 shape）
    private static Set<ChunkPos> dilate(Set<ChunkPos> shape, int k) {
        Set<ChunkPos> current = new HashSet<>(shape);
        Set<ChunkPos> next = new HashSet<>();
        for (int layer = 0; layer < k; layer++) {
            next.clear();
            next.addAll(current);
            for (ChunkPos p : current) {
                next.add(new ChunkPos(p.x() + 1, p.z()));
                next.add(new ChunkPos(p.x() - 1, p.z()));
                next.add(new ChunkPos(p.x(), p.z() + 1));
                next.add(new ChunkPos(p.x(), p.z() - 1));
            }
            var tmp = current;
            current = next;
            next = tmp;
        }
        return current;
    }

    // ============================================================
    // 查找附近 n 个指定类型的区块（螺旋搜索）
    // ============================================================

    /**
     * 以 center 为中心螺旋搜索，直到找到 n 个匹配 zoneMask 的区块。
     *
     * @param zones    稀疏 zone 数据
     * @param center   搜索中心
     * @param n        目标数量
     * @param zoneMask 要匹配的 ZoneType 位掩码（通过 {@link ZoneType#of(ZoneType, ZoneType...)} 组合）
     * @param maxR     最大搜索半径（防止无限扩张），超过则返回已找到的
     * @return 找到的 ChunkPos 列表（按离中心距离升序排列）
     */
    public static List<ChunkPos> findNearby(Map<ChunkPos, ZoneType> zones,
                                             ChunkPos center, int n, byte zoneMask, int maxR) {
        List<ChunkPos> found = new ArrayList<>(n);
        if (n <= 0 || zones.isEmpty()) return found;

        int cx = center.x(), cz = center.z();
        for (int r = 0; r <= maxR && found.size() < n; r++) {
            // 环 r: 所有满足 max(|dx|,|dz|) == r 的区块
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue; // 不在环上，是内部点
                    if (found.size() >= n) return found;
                    ChunkPos p = new ChunkPos(cx + dx, cz + dz);
                    ZoneType t = zones.get(p);
                    if (t != null && t.matches(zoneMask)) {
                        found.add(p);
                    }
                }
            }
        }
        return found;
    }

    // ============================================================
    // 带保护的类型变更
    // ============================================================

    /**
     * 将 center 周围 radius 内的区域按 pattern 生成器变更类型。
     * 受 protectMask 保护的现有类型不会被修改。
     *
     * @param zones       稀疏 zone 数据（会被直接修改）
     * @param center      中心区块
     * @param radius      半径
     * @param pattern     新类型生成规则（输入 dx,dz，返回目标 ZoneType；返回 null 表示跳过）
     * @param protectMask 受保护的类型掩码（例如 Safe_Zone.mask()），匹配到的区块不会被修改
     * @return 实际被修改的 ChunkPos 列表
     */
    public static List<ChunkPos> paintWithProtect(Map<ChunkPos, ZoneType> zones,
                                                   ChunkPos center, int radius,
                                                   ZonePattern pattern, byte protectMask) {
        List<ChunkPos> modified = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos p = new ChunkPos(center.x() + dx, center.z() + dz);
                ZoneType existing = zones.get(p);
                if (existing != null && existing.matches(protectMask)) continue;

                ZoneType target = pattern.apply(dx, dz);
                if (target == null) continue;
                if (existing == target) continue;

                zones.put(p, target);
                modified.add(p);
            }
        }
        return modified;
    }

    /** 区域类型变更的模式生成器。 */
    @FunctionalInterface
    public interface ZonePattern {
        /** @param dx 相对于中心的 x 偏移
         *  @param dz 相对于中心的 z 偏移
         *  @return 目标 ZoneType，返回 null 表示跳过此位置 */
        ZoneType apply(int dx, int dz);
    }

    /**
     * 将 center 为中心 radius 半径内全部设置为 targetType，受 protectMask 保护。
     * {@link #paintWithProtect} 的便捷重载，使用常值 pattern。
     */
    public static List<ChunkPos> fillWithProtect(Map<ChunkPos, ZoneType> zones,
                                                  ChunkPos center, int radius,
                                                  ZoneType targetType, byte protectMask) {
        return paintWithProtect(zones, center, radius, (dx, dz) -> targetType, protectMask);
    }

    // ============================================================
    // 补充工具
    // ============================================================

    /**
     * 判断两个 ChunkPos 是否曼哈顿邻接（4邻域）。
     */
    public static boolean isAdjacent(ChunkPos a, ChunkPos b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.z() - b.z()) == 1;
    }

    /**
     * 返回指定区块的四邻域。
     */
    public static Set<ChunkPos> neighbors4(ChunkPos p) {
        return Set.of(
                new ChunkPos(p.x() + 1, p.z()),
                new ChunkPos(p.x() - 1, p.z()),
                new ChunkPos(p.x(), p.z() + 1),
                new ChunkPos(p.x(), p.z() - 1)
        );
    }

    /**
     * 计算两个区块之间的欧几里得距离。
     */
    public static double distance(ChunkPos a, ChunkPos b) {
        long dx = (long) a.x() - b.x();
        long dz = (long) a.z() - b.z();
        return Math.sqrt(dx * dx + dz * dz);
    }

    // ============================================================
    // 包围盒
    // ============================================================

    /** 一组区块的轴对齐包围盒。 */
    public record Bounds(int minX, int minZ, int maxX, int maxZ) {
        public Bounds expanded(int r) {
            return new Bounds(minX - r, minZ - r, maxX + r, maxZ + r);
        }
        public boolean contains(ChunkPos p) {
            return p.x() >= minX && p.x() <= maxX && p.z() >= minZ && p.z() <= maxZ;
        }
        /** 返回包围盒内所有区块坐标。 */
        public Set<ChunkPos> allChunks() {
            Set<ChunkPos> result = new LinkedHashSet<>((maxX - minX + 1) * (maxZ - minZ + 1));
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    result.add(new ChunkPos(x, z));
                }
            }
            return result;
        }
    }

    /** 从区块集合计算轴对齐包围盒。 */
    public static Bounds boundsOf(Collection<ChunkPos> chunks) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (ChunkPos p : chunks) {
            if (p.x() < minX) minX = p.x();
            if (p.x() > maxX) maxX = p.x();
            if (p.z() < minZ) minZ = p.z();
            if (p.z() > maxZ) maxZ = p.z();
        }
        return new Bounds(minX, minZ, maxX, maxZ);
    }

    // ============================================================
    // 掩码过滤
    // ============================================================

    /**
     * 从 zone entries 中筛选匹配 mask 的区块，同时返回包围盒。
     * 一次遍历完成筛选 + 包围盒计算。
     */
    public static FilteredChunks filterByMask(Set<Map.Entry<ChunkPos, ZoneType>> entries, byte mask) {
        Set<ChunkPos> chunks = new HashSet<>();
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (var e : entries) {
            if (!e.getValue().matches(mask)) continue;
            ChunkPos p = e.getKey();
            chunks.add(p);
            if (p.x() < minX) minX = p.x();
            if (p.x() > maxX) maxX = p.x();
            if (p.z() < minZ) minZ = p.z();
            if (p.z() > maxZ) maxZ = p.z();
        }
        return new FilteredChunks(chunks, new Bounds(minX, minZ, maxX, maxZ));
    }

    /** 筛选 + 包围盒 的结果。 */
    public record FilteredChunks(Set<ChunkPos> chunks, Bounds bounds) {
        public boolean isEmpty() { return chunks.isEmpty(); }
    }

    // ============================================================
    // 连通分量
    // ============================================================

    /**
     * 将区块集合按四邻域连通性拆分为连通分量。
     * 用于在大片区域中找出独立的"噪点"区域（如 Active_Zone 内部的 Node_Zone 孤岛）。
     *
     * @param chunks 区块集合
     * @return 连通分量列表，每个分量是一个相互邻接的区块集合
     */
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
                ChunkPos p = queue.poll();
                component.add(p);
                for (ChunkPos n : neighbors4(p)) {
                    if (remaining.remove(n)) {
                        queue.add(n);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }
}
