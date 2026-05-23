package org.galaxy.beyond.api.system.zone.algorithm;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Zone 扩张器 —— 泛型化，支持任意坐标类型和扩张策略。
 *
 * @param <P> 位置坐标类型（如 ChunkPos、BlockPos）
 */
public interface ZoneExpander<P> {

    /**
     * 从种子集合出发，在 {@code radius} 范围内扩张。
     *
     * @param level     目标维度
     * @param seeds     种子坐标集合
     * @param radius    扩张半径
     * @param zoneType  要设置的 ZoneType
     * @param setZone   写操作：（坐标，类型）→ 是否成功
     * @param getZone   读操作：坐标 → 已有类型（null 表示空地）
     * @return 是否有新区块被添加
     */
    boolean expand(ServerLevel level, Set<P> seeds, int radius, ZoneType zoneType,
                   BiFunction<P, ZoneType, Boolean> setZone, Function<P, ZoneType> getZone);

    /** 计算种子到目标的距离 */
    double distance(P seed, P target);
}
