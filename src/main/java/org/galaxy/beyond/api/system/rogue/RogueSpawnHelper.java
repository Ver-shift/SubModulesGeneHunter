package org.galaxy.beyond.api.system.rogue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.block.NodeBlock;

public final class RogueSpawnHelper {

    private static final int[] Y_OFFSETS = {0, 1, -1, 2, -2, 3, -3};

    private RogueSpawnHelper() {
    }

    public static Vec3 gatewayPos(ServerLevel level, BlockPos nodePos, int distance) {
        BlockPos base = nodeBase(level, nodePos);
        BlockPos target = base.relative(nodeFacing(level, base), distance);
        BlockPos spawnPos = findGroundNear(level, target, base.getY(), 2);
        if (spawnPos == null) spawnPos = target.atY(base.getY());
        return new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
    }

    public static BlockPos findGroundNear(ServerLevel level, BlockPos preferred, int anchorY, int radius) {
        for (int range = 0; range <= radius; range++) {
            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != range) continue;
                    BlockPos result = findGroundAt(level, preferred.offset(dx, 0, dz), anchorY);
                    if (result != null) return result;
                }
            }
        }
        return null;
    }

    public static BlockPos nodeBase(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof NodeBlock
                && state.getValue(NodeBlock.HALF) == DoubleBlockHalf.UPPER) {
            return pos.below();
        }
        return pos;
    }

    public static Direction nodeFacing(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof NodeBlock) {
            return state.getValue(NodeBlock.FACING);
        }
        return Direction.NORTH;
    }

    private static BlockPos findGroundAt(ServerLevel level, BlockPos pos, int anchorY) {
        for (int offset : Y_OFFSETS) {
            BlockPos feet = new BlockPos(pos.getX(), anchorY + offset, pos.getZ());
            if (canStand(level, feet)) return feet;
        }
        return null;
    }

    private static boolean canStand(ServerLevel level, BlockPos feet) {
        return hasCollision(level, feet.below())
                && isEmpty(level, feet)
                && isEmpty(level, feet.above());
    }

    private static boolean hasCollision(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isEmpty(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty();
    }
}
