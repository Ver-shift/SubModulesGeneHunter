package org.galaxy.beyond.api.system.zone.algorithm;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 圆形扩张器 —— 使用欧几里得距离，生成真正的圆形区域。
 * <p>
 * 算法：计算种子包围盒 → 扩大 radius → 遍历盒内所有坐标，距任一种子 ≤ radius 的加入。
 */
public class CircularExpander implements ZoneExpander<ChunkPos> {

    @Override
    public boolean expand(ServerLevel level, Set<ChunkPos> seeds, int radius, ZoneType zoneType,
                          BiFunction<ChunkPos, ZoneType, Boolean> setZone, Function<ChunkPos, ZoneType> getZone) {
        if (seeds.isEmpty()) return false;

        // 包围盒
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (ChunkPos s : seeds) {
            int x = toInt(s);
            int z = toIntZ(s);
            if (x < minX) minX = x; if (z < minZ) minZ = z;
            if (x > maxX) maxX = x; if (z > maxZ) maxZ = z;
        }
        minX -= radius; minZ -= radius;
        maxX += radius; maxZ += radius;

        int r2 = radius * radius;
        boolean changed = false;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                // 已占用 → 跳过
                ChunkPos p = new ChunkPos(x, z);
                if (getZone.apply(p) != null) continue;

                // 距任一种子 ≤ radius
                if (minDist2(x, z, seeds) > r2) continue;

                if (setZone.apply(p, zoneType)) changed = true;
            }
        }
        return changed;
    }

    private static int minDist2(int x, int z, Set<ChunkPos> seeds) {
        int min = Integer.MAX_VALUE;
        for (ChunkPos s : seeds) {
            int dx = x - toInt(s);
            int dz = z - toIntZ(s);
            int d2 = dx * dx + dz * dz;
            if (d2 < min) min = d2;
        }
        return min;
    }

    @Override
    public double distance(ChunkPos seed, ChunkPos target) {
        int dx = toInt(seed) - toInt(target);
        int dz = toIntZ(seed) - toIntZ(target);
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static int toInt(ChunkPos p) { return p.getMinBlockX() >> 4; }
    private static int toIntZ(ChunkPos p) { return p.getMinBlockZ() >> 4; }
}
