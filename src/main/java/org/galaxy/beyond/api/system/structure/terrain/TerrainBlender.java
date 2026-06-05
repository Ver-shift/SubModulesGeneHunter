package org.galaxy.beyond.api.system.structure.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.ArrayList;
import java.util.List;

public final class TerrainBlender {

    private TerrainBlender() {
    }

    public static void blend(WorldGenLevel level, BoundingBox structureBox, ChunkPos chunkPos,
                             PiecesContainer pieces, TerrainBlendConfig config) {
        TagKey<Block> solidTag = TagKey.create(Registries.BLOCK, config.solidTag());
        BoundingBox chunkBox = new BoundingBox(
                chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), level.getMaxY(), chunkPos.getMaxBlockZ()
        );
        List<BlockPos> anchors = collectAnchors(level, pieces, solidTag, config.depth());
        if (anchors.isEmpty()) return;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minX = Math.max(chunkBox.minX(), structureBox.minX() - config.radius());
        int maxX = Math.min(chunkBox.maxX(), structureBox.maxX() + config.radius());
        int minZ = Math.max(chunkBox.minZ(), structureBox.minZ() - config.radius());
        int maxZ = Math.min(chunkBox.maxZ(), structureBox.maxZ() + config.radius());
        int radiusSq = config.radius() * config.radius();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int nearestSq = nearestHorizontalDistanceSq(anchors, x, z, radiusSq);
                if (nearestSq > radiusSq) continue;

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                if (surfaceY <= level.getMinY()) continue;
                mutable.set(x, surfaceY, z);
                BlockState surface = level.getBlockState(mutable);
                if (!surface.is(Blocks.GRASS_BLOCK) && !surface.is(Blocks.DIRT) && !surface.is(Blocks.STONE)
                        && !surface.is(Blocks.WATER)) {
                    continue;
                }

                double factor = 1.0 - (Math.sqrt(nearestSq) / config.radius());
                placeBlendSurface(level, mutable, randomLike(x, z), factor);
            }
        }
    }

    private static List<BlockPos> collectAnchors(WorldGenLevel level, PiecesContainer pieces,
                                                 TagKey<Block> solidTag, int depth) {
        List<BlockPos> anchors = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (StructurePiece piece : pieces.pieces()) {
            BoundingBox box = piece.getBoundingBox();
            int minY = Math.max(level.getMinY(), box.minY());
            int maxY = Math.min(level.getMaxY(), box.minY() + depth);
            for (int x = box.minX(); x <= box.maxX(); x++) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    for (int y = minY; y <= maxY; y++) {
                        mutable.set(x, y, z);
                        if (level.getBlockState(mutable).is(solidTag)) {
                            anchors.add(new BlockPos(x, y, z));
                            break;
                        }
                    }
                }
            }
        }
        return anchors;
    }

    private static int nearestHorizontalDistanceSq(List<BlockPos> anchors, int x, int z, int cutoffSq) {
        int best = cutoffSq + 1;
        for (BlockPos anchor : anchors) {
            int dx = anchor.getX() - x;
            int dz = anchor.getZ() - z;
            int dist = dx * dx + dz * dz;
            if (dist < best) {
                best = dist;
                if (best == 0) return 0;
            }
        }
        return best;
    }

    private static void placeBlendSurface(WorldGenLevel level, BlockPos.MutableBlockPos pos, int salt, double factor) {
        BlockState state;
        int pick = Math.floorMod(salt, 100);
        if (factor > 0.72) {
            state = pick < 45 ? Blocks.COARSE_DIRT.defaultBlockState()
                    : pick < 75 ? Blocks.STONE.defaultBlockState()
                    : Blocks.ANDESITE.defaultBlockState();
        } else if (factor > 0.38) {
            state = pick < 55 ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : pick < 78 ? Blocks.COARSE_DIRT.defaultBlockState()
                    : Blocks.MOSS_BLOCK.defaultBlockState();
        } else {
            state = pick < 70 ? Blocks.GRASS_BLOCK.defaultBlockState()
                    : pick < 86 ? Blocks.MOSS_CARPET.defaultBlockState()
                    : Blocks.STONE.defaultBlockState();
        }
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    private static int randomLike(int x, int z) {
        int h = x * 73428767 ^ z * 91227153;
        h ^= h >>> 13;
        h *= 1274126177;
        return h;
    }
}
