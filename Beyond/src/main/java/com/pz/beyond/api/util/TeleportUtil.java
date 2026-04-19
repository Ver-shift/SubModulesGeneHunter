package com.pz.beyond.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * 传送工具类
 * <p>
 * 提供安全传送位置查找和结构定位功能
 * 参考 Biome-Spawn-Point 算法设计
 */
public class TeleportUtil {

    /**
     * 查找安全的传送位置（确保玩家不会卡在方块里）
     * <p>
     * 改进算法：
     * 1. 使用 MOTION_BLOCKING_NO_LEAVES heightmap 避免出生在树上
     * 2. 在更大范围内搜索安全位置
     * 3. 优先寻找固体地面，而非树叶/水面
     *
     * @param level  服务器世界
     * @param target 目标位置
     * @return 安全的传送位置
     */
    public static BlockPos findSafeTeleportPos(ServerLevel level, BlockPos target) {
        // 首先尝试目标位置本身
        if (isSafeStandingPosition(level, target)) {
            return target;
        }
        
        // 使用 MOTION_BLOCKING_NO_LEAVES 获取地表高度（避免树叶）
        BlockPos surfacePos = getSurfacePos(level, target);
        
        // 在更大范围内搜索安全位置
        for (int offset = 0; offset <= 20; offset++) {
            // 向上搜索
            BlockPos up = surfacePos.above(offset);
            if (isSafeStandingPosition(level, up)) {
                return up;
            }
            
            // 向下搜索（跳过offset=0避免重复）
            if (offset > 0) {
                BlockPos down = surfacePos.below(offset);
                if (isSafeStandingPosition(level, down)) {
                    return down;
                }
            }
        }
        
        // 如果找不到理想位置，尝试强制找到一个可站立位置
        BlockPos forcedPos = findForcedSafePos(level, surfacePos);
        if (forcedPos != null) {
            return forcedPos;
        }
        
        // 最后回退：返回地表上方并确保脚下有方块
        return ensureSolidGround(level, surfacePos.above());
    }

    /**
     * 获取地表位置（不包含树叶）
     */
    public static BlockPos getSurfacePos(ServerLevel level, BlockPos pos) {
        // 使用 MOTION_BLOCKING_NO_LEAVES 避免出生在树上
        int surfaceY = level.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                pos
        ).getY();
        return new BlockPos(pos.getX(), surfaceY, pos.getZ());
    }

    /**
     * 检查位置是否可以安全站立
     * <p>
     * 改进条件：
     * - 脚下是固体方块（非空气、非液体、非树叶）
     * - 脚下、头部都是空气或可穿越方块
     * - 优先选择能看到天空的位置
     *
     * @param level 服务器世界
     * @param pos   检查位置
     * @return 是否安全
     */
    public static boolean isSafeStandingPosition(ServerLevel level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        // 脚下必须是固体方块（排除空气、液体、树叶、火等）
        boolean groundSolid = isValidGround(belowState);

        // 脚下和头部必须是空气或可穿越方块
        boolean hasSpace = feetState.isAir() &&
                (headState.isAir() || !headState.blocksMotion());

        // 必须露天，防止出生在洞里或建筑内部
        boolean openSky = hasOpenSky(level, pos);

        return groundSolid && hasSpace && openSky;
    }

    /**
     * 检查方块是否是有效的地面
     */
    private static boolean isValidGround(BlockState state) {
        // 排除空气
        if (state.isAir()) return false;
        
        // 排除液体
        if (state.liquid()) return false;
        
        // 排除树叶
        if (state.is(net.minecraft.tags.BlockTags.LEAVES)) return false;
        
        // 排除火、岩浆等危险方块
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.LAVA)) return false;
        
        // 排除仙人掌等会伤害玩家的方块
        if (state.is(Blocks.CACTUS)) return false;
        
        // 必须是固体方块
        return state.blocksMotion();
    }

    /**
     * 强制查找安全位置（降低要求）
     */
    private static BlockPos findForcedSafePos(ServerLevel level, BlockPos center) {
        // 在水平方向扩大搜索范围
        for (int radius = 1; radius <= 10; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // 只检查外圈
                    if (Math.abs(dx) < radius && Math.abs(dz) < radius) continue;
                    
                    BlockPos checkPos = center.offset(dx, 0, dz);
                    BlockPos surfacePos = getSurfacePos(level, checkPos);
                    
                    // 降低要求：只需要脚下有固体方块，头顶有空间
                    if (isBasicSafePos(level, surfacePos)) {
                        return surfacePos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 基本安全位置检查（最低要求）
     */
    private static boolean isBasicSafePos(ServerLevel level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        boolean hasGround = !belowState.isAir() && !belowState.liquid() && belowState.blocksMotion();
        boolean hasSpace = feetState.isAir() && headState.isAir();
        boolean openSky = hasOpenSky(level, pos);

        return hasGround && hasSpace && openSky;
    }

    /**
     * 确保脚下有固体方块
     */
    private static BlockPos ensureSolidGround(ServerLevel level, BlockPos pos) {
        for (int y = pos.getY(); y > level.getMinBuildHeight() + 1; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isBasicSafePos(level, checkPos)) {
                return checkPos;
            }
        }

        for (int y = pos.getY(); y < level.getMaxBuildHeight() - 1; y++) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isBasicSafePos(level, checkPos)) {
                return checkPos;
            }
        }

        // 回退到地表上方，仍然尽量保持露天
        BlockPos surface = getSurfacePos(level, pos).above();
        return hasOpenSky(level, surface) ? surface : pos;
    }

    private static boolean hasOpenSky(ServerLevel level, BlockPos pos) {
        // 头顶必须无遮蔽
        return level.canSeeSky(pos.above());
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
