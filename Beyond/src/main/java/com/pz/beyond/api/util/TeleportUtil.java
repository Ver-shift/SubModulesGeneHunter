package com.pz.beyond.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * 传送工具类
 * <p>
 * 提供安全传送位置查找和结构定位功能
 */
public class TeleportUtil {

    /**
     * 查找安全的传送位置（确保玩家不会卡在方块里）
     * <p>
     * 条件：
     * - 脚下是固体方块（非空气、非液体）
     * - 脚下、头部都是空气
     * - 能看到天空（确保不在地下或室内）
     *
     * @param level  服务器世界
     * @param target 目标位置
     * @return 安全的传送位置
     */
    public static BlockPos findSafeTeleportPos(ServerLevel level, BlockPos target) {
        // 从目标位置向上查找安全位置
        for (int y = target.getY(); y < level.getMaxBuildHeight() - 2; y++) {
            BlockPos checkPos = new BlockPos(target.getX(), y, target.getZ());
            if (isSafeStandingPosition(level, checkPos)) {
                return checkPos;
            }
        }
        // 找不到则返回目标位置上方
        return target.above();
    }

    /**
     * 检查位置是否可以安全站立
     * <p>
     * 条件：
     * - 脚下是固体方块（非空气、非液体）
     * - 脚下、头部都是空气
     * - 能看到天空（确保不在地下或室内）
     *
     * @param level 服务器世界
     * @param pos   检查位置
     * @return 是否安全
     */
    public static boolean isSafeStandingPosition(ServerLevel level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        // 脚下是固体方块（非空气、非液体）
        boolean groundSolid = !belowState.isAir() && !belowState.liquid();
        // 脚下和头部是空气
        boolean hasSpace = feetState.isAir() && headState.isAir();
        // 能看到天空（头顶无任何方块遮挡）
        boolean canSeeSky = level.canSeeSky(pos);

        return groundSolid && hasSpace && canSeeSky;
    }

    /**
     * 查找最近的结构位置
     *
     * @param level         服务器世界
     * @param structureTag  结构标签
     * @param searchRadius  搜索半径（区块）
     * @return 结构位置，未找到则返回 null
     */
    public static BlockPos findNearestStructure(ServerLevel level, TagKey<Structure> structureTag, int searchRadius) {
        BlockPos fallback = level.getSharedSpawnPos();
        return level.findNearestMapStructure(structureTag, fallback, searchRadius, false);
    }

    /**
     * 查找最近的结构位置（带默认返回）
     *
     * @param level         服务器世界
     * @param structureTag  结构标签
     * @param searchRadius  搜索半径（区块）
     * @param defaultPos    默认返回位置
     * @return 结构位置，未找到则返回 defaultPos
     */
    public static BlockPos findNearestStructure(ServerLevel level, TagKey<Structure> structureTag, int searchRadius, BlockPos defaultPos) {
        BlockPos found = findNearestStructure(level, structureTag, searchRadius);
        return found != null ? found : defaultPos;
    }
}
