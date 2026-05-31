package org.galaxy.beyond.api.system.structure.terrain;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.mixin.SinglePoolElementAccessor;
import org.galaxy.beyond.mixin.StructureTemplateAccessor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TerrainBlender {

    private static final Logger LOGGER = LogUtils.getLogger();

    private TerrainBlender() {
    }

    public static void blend(WorldGenLevel level, BoundingBox placeBox, ChunkPos chunkPos,
                             PiecesContainer pieces, TerrainBlendConfig config) {
        TagKey<Block> solidTag = TagKey.create(Registries.BLOCK, config.solidTag());
        BoundingBox chunkBox = new BoundingBox(
                chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ()
        );
        BoundingBox structureBox = pieces.calculateBoundingBox();
        BoundingBox targetBox = intersect(chunkBox, placeBox);
        if (targetBox == null) return;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int blendRadius = config.blendRadius();
        int minX = Math.max(targetBox.minX(), structureBox.minX() - blendRadius);
        int maxX = Math.min(targetBox.maxX(), structureBox.maxX() + blendRadius);
        int minZ = Math.max(targetBox.minZ(), structureBox.minZ() - blendRadius);
        int maxZ = Math.min(targetBox.maxZ(), structureBox.maxZ() + blendRadius);
        if (minX > maxX || minZ > maxZ) return;

        int blendRadiusSq = blendRadius * blendRadius;
        BoundingBox anchorSearchBox = new BoundingBox(
                structureBox.minX() - blendRadius, targetBox.minY(), structureBox.minZ() - blendRadius,
                structureBox.maxX() + blendRadius, targetBox.maxY(), structureBox.maxZ() + blendRadius
        );
        List<BlockPos> anchors = collectAnchors(level, pieces, solidTag, config, anchorSearchBox);
        if (anchors.isEmpty()) {
            LOGGER.info("Terrain blend skipped for chunk {}: no anchors in {}", chunkPos, config.solidTag());
            return;
        }

        int changed = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                AnchorDistance nearest = nearestHorizontalDistance(anchors, x, z, blendRadiusSq);
                if (nearest.distanceSq() > blendRadiusSq) continue;

                int anchorY = nearest.anchor().getY();
                boolean anchorColumn = nearest.distanceSq() == 0;
                int surfaceY = findTerrainSurfaceY(level, mutable, x, z, anchorColumn ? anchorY - 1 : level.getMaxBuildHeight() - 1);
                if (surfaceY <= level.getMinBuildHeight()) continue;

                double factor = 1.0 - (Math.sqrt(nearest.distanceSq()) / blendRadius);
                ColumnMaterial material = sampleColumnMaterial(level, mutable, x, z, surfaceY);
                int depth = Math.max(1, (int) Math.ceil(config.foundationDepth() * factor));
                changed += fillSurfaceFluids(level, mutable, x, z, surfaceY, depth, material);
                double distance = Math.sqrt(nearest.distanceSq());
                if (anchorColumn) {
                    int minFoundationY = foundationTargetY(level, mutable, x, z, anchorY, surfaceY, config);
                    changed += fillFoundation(level, mutable, x, z, anchorY - 1, minFoundationY, material);
                } else {
                    changed += fillTerrainShoulder(level, mutable, x, z, surfaceY, anchorY, distance, config, material);
                }
            }
        }
        LOGGER.info("Terrain blend applied for chunk {}: anchors={}, changed={}, radius={}, depth={}",
                chunkPos, anchors.size(), changed, config.radius(), config.depth());
        Beyond.debugInfo("Terrain blend structure chunk {} anchors={} changed={}", chunkPos, anchors.size(), changed);
    }

    private static BoundingBox intersect(BoundingBox first, BoundingBox second) {
        int minX = Math.max(first.minX(), second.minX());
        int minY = Math.max(first.minY(), second.minY());
        int minZ = Math.max(first.minZ(), second.minZ());
        int maxX = Math.min(first.maxX(), second.maxX());
        int maxY = Math.min(first.maxY(), second.maxY());
        int maxZ = Math.min(first.maxZ(), second.maxZ());
        return minX > maxX || minY > maxY || minZ > maxZ
                ? null
                : new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static List<BlockPos> collectAnchors(WorldGenLevel level, PiecesContainer pieces, TagKey<Block> solidTag,
                                                 TerrainBlendConfig config, BoundingBox searchBox) {
        List<BlockPos> anchors = config.anchorMode() == TerrainBlendConfig.AnchorMode.FOOTPRINT
                ? collectTemplateFootprintAnchors(level, pieces, config.depth(), searchBox)
                : collectTemplateSolidAnchors(level, pieces, solidTag, config.depth(), searchBox);
        if (!anchors.isEmpty()) {
            return anchors;
        }
        return config.anchorMode() == TerrainBlendConfig.AnchorMode.FOOTPRINT
                ? collectPlacedFootprintAnchors(level, pieces, config.depth(), searchBox)
                : collectPlacedSolidAnchors(level, pieces, solidTag, config.depth(), searchBox);
    }

    private static List<BlockPos> collectTemplateSolidAnchors(WorldGenLevel level, PiecesContainer pieces,
                                                              TagKey<Block> solidTag, int depth, BoundingBox searchBox) {
        Map<Long, BlockPos> anchors = new HashMap<>();
        for (StructurePiece piece : pieces.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)
                    || !(poolPiece.getElement() instanceof SinglePoolElement singlePoolElement)) {
                continue;
            }

            StructureTemplate template = ((SinglePoolElementAccessor) singlePoolElement)
                    .beyond$invokeGetTemplate(level.getLevel().getStructureManager());
            List<StructureTemplate.Palette> palettes = ((StructureTemplateAccessor) template).beyond$getPalettes();
            if (palettes.isEmpty()) continue;

            List<StructureTemplate.StructureBlockInfo> blocks = palettes.getFirst().blocks();
            Vec3i size = template.getSize();
            int minTemplateY = findLowestSolidTemplateY(blocks, solidTag);
            if (minTemplateY < 0) continue;

            int maxTemplateY = Math.min(size.getY() - 1, minTemplateY + depth - 1);
            if (maxTemplateY < 0) continue;

            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(poolPiece.getRotation());
            BlockPos origin = poolPiece.getPosition();
            for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
                BlockPos relative = blockInfo.pos();
                if (relative.getY() < minTemplateY || relative.getY() > maxTemplateY || !blockInfo.state().is(solidTag))
                    continue;

                BlockPos anchor = StructureTemplate.calculateRelativePosition(settings, relative).offset(origin);
                if (searchBox.isInside(anchor)) {
                    addLowestAnchor(anchors, anchor);
                }
            }
        }
        return new ArrayList<>(anchors.values());
    }

    private static List<BlockPos> collectTemplateFootprintAnchors(WorldGenLevel level, PiecesContainer pieces,
                                                                  int depth, BoundingBox searchBox) {
        Map<Long, BlockPos> anchors = new HashMap<>();
        for (StructurePiece piece : pieces.pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)
                    || !(poolPiece.getElement() instanceof SinglePoolElement singlePoolElement)) {
                continue;
            }

            StructureTemplate template = ((SinglePoolElementAccessor) singlePoolElement)
                    .beyond$invokeGetTemplate(level.getLevel().getStructureManager());
            List<StructureTemplate.Palette> palettes = ((StructureTemplateAccessor) template).beyond$getPalettes();
            if (palettes.isEmpty()) continue;

            List<StructureTemplate.StructureBlockInfo> blocks = palettes.getFirst().blocks();
            Vec3i size = template.getSize();
            int minTemplateY = findLowestFootprintTemplateY(blocks);
            if (minTemplateY < 0) continue;

            int maxTemplateY = Math.min(size.getY() - 1, minTemplateY + depth - 1);
            if (maxTemplateY < 0) continue;

            StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(poolPiece.getRotation());
            BlockPos origin = poolPiece.getPosition();
            for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
                BlockPos relative = blockInfo.pos();
                if (relative.getY() < minTemplateY || relative.getY() > maxTemplateY
                        || !isFootprintBlock(blockInfo.state())) {
                    continue;
                }

                BlockPos anchor = StructureTemplate.calculateRelativePosition(settings, relative).offset(origin);
                if (searchBox.isInside(anchor)) {
                    addLowestAnchor(anchors, anchor);
                }
            }
        }
        return new ArrayList<>(anchors.values());
    }

    private static int findLowestSolidTemplateY(List<StructureTemplate.StructureBlockInfo> blocks, TagKey<Block> solidTag) {
        int minY = Integer.MAX_VALUE;
        for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
            if (blockInfo.state().is(solidTag) && blockInfo.pos().getY() < minY) {
                minY = blockInfo.pos().getY();
            }
        }
        return minY == Integer.MAX_VALUE ? -1 : minY;
    }

    private static int findLowestFootprintTemplateY(List<StructureTemplate.StructureBlockInfo> blocks) {
        int minY = Integer.MAX_VALUE;
        for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
            if (isFootprintBlock(blockInfo.state()) && blockInfo.pos().getY() < minY) {
                minY = blockInfo.pos().getY();
            }
        }
        return minY == Integer.MAX_VALUE ? -1 : minY;
    }

    private static List<BlockPos> collectPlacedSolidAnchors(WorldGenLevel level, PiecesContainer pieces,
                                                            TagKey<Block> solidTag, int depth, BoundingBox searchBox) {
        Map<Long, BlockPos> anchors = new HashMap<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (StructurePiece piece : pieces.pieces()) {
            BoundingBox box = piece.getBoundingBox();
            int minX = Math.max(box.minX(), searchBox.minX());
            int maxX = Math.min(box.maxX(), searchBox.maxX());
            int minZ = Math.max(box.minZ(), searchBox.minZ());
            int maxZ = Math.min(box.maxZ(), searchBox.maxZ());
            if (minX > maxX || minZ > maxZ) continue;

            int minY = Math.max(Math.max(level.getMinBuildHeight(), box.minY()), searchBox.minY());
            int maxY = Math.min(Math.min(level.getMaxBuildHeight() - 1, box.minY() + depth - 1), searchBox.maxY());
            if (minY > maxY) continue;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        mutable.set(x, y, z);
                        if (level.getBlockState(mutable).is(solidTag)) {
                            addLowestAnchor(anchors, new BlockPos(x, y, z));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(anchors.values());
    }

    private static List<BlockPos> collectPlacedFootprintAnchors(WorldGenLevel level, PiecesContainer pieces,
                                                                int depth, BoundingBox searchBox) {
        Map<Long, BlockPos> anchors = new HashMap<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (StructurePiece piece : pieces.pieces()) {
            BoundingBox box = piece.getBoundingBox();
            int minX = Math.max(box.minX(), searchBox.minX());
            int maxX = Math.min(box.maxX(), searchBox.maxX());
            int minZ = Math.max(box.minZ(), searchBox.minZ());
            int maxZ = Math.min(box.maxZ(), searchBox.maxZ());
            if (minX > maxX || minZ > maxZ) continue;

            int minY = Math.max(Math.max(level.getMinBuildHeight(), box.minY()), searchBox.minY());
            int maxY = Math.min(Math.min(level.getMaxBuildHeight() - 1, box.minY() + depth - 1), searchBox.maxY());
            if (minY > maxY) continue;

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        mutable.set(x, y, z);
                        if (isFootprintBlock(level.getBlockState(mutable))) {
                            addLowestAnchor(anchors, new BlockPos(x, y, z));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(anchors.values());
    }

    private static void addLowestAnchor(Map<Long, BlockPos> anchors, BlockPos anchor) {
        long key = BlockPos.asLong(anchor.getX(), 0, anchor.getZ());
        BlockPos existing = anchors.get(key);
        if (existing == null || anchor.getY() < existing.getY()) {
            anchors.put(key, anchor);
        }
    }

    private static AnchorDistance nearestHorizontalDistance(List<BlockPos> anchors, int x, int z, int cutoffSq) {
        int best = cutoffSq + 1;
        BlockPos nearest = BlockPos.ZERO;
        for (BlockPos anchor : anchors) {
            int dx = anchor.getX() - x;
            int dz = anchor.getZ() - z;
            int dist = dx * dx + dz * dz;
            if (dist < best) {
                best = dist;
                nearest = anchor;
                if (best == 0) return new AnchorDistance(nearest, 0);
            }
        }
        return new AnchorDistance(nearest, best);
    }

    private static int findTerrainSurfaceY(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int maxY) {
        int minY = level.getMinBuildHeight();
        int surfaceY = Math.min(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1, maxY);
        for (int y = surfaceY; y >= minY; y--) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || isLoosePlant(state)) continue;
            return y;
        }
        return minY;
    }

    private static ColumnMaterial sampleColumnMaterial(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                                                       int x, int z, int startY) {
        BlockState top = null;
        BlockState filler = null;
        int minY = level.getMinBuildHeight();
        for (int y = Math.min(startY, level.getMaxBuildHeight() - 1); y >= minY; y--) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!isBlendMaterial(state)) continue;

            if (top == null) {
                top = state;
            } else {
                filler = state;
                break;
            }
        }

        if (top == null) {
            top = Blocks.DIRT.defaultBlockState();
        }
        if (filler == null) {
            filler = defaultFiller(top);
        }
        return new ColumnMaterial(top, filler);
    }

    private static int fillSurfaceFluids(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                                         int x, int z, int surfaceY, int depth, ColumnMaterial material) {
        pos.set(x, surfaceY, z);
        if (level.getBlockState(pos).getFluidState().isEmpty()) return 0;

        int minY = Math.max(level.getMinBuildHeight(), surfaceY - depth + 1);
        int changed = 0;
        boolean top = true;
        for (int y = surfaceY; y >= minY; y--) {
            pos.set(x, y, z);
            BlockState current = level.getBlockState(pos);
            if (isFillBlocked(current)) break;
            level.setBlock(pos, top ? material.top() : material.filler(), Block.UPDATE_CLIENTS);
            changed += clearUnsupportedPlant(level, pos, x, y + 1, z);
            changed++;
            top = false;
        }
        return changed;
    }

    private static int fillFoundation(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                                      int x, int z, int startY, int minY, ColumnMaterial material) {
        int changed = 0;
        int bottomY = Math.max(level.getMinBuildHeight(), minY);
        for (int y = Math.min(startY, level.getMaxBuildHeight() - 1); y >= bottomY; y--) {
            pos.set(x, y, z);
            BlockState current = level.getBlockState(pos);
            if (isFoundationBlocked(current)) break;
            level.setBlock(pos, material.filler(), Block.UPDATE_CLIENTS);
            changed += clearUnsupportedPlant(level, pos, x, y + 1, z);
            changed++;
        }
        return changed;
    }

    private static int fillTerrainShoulder(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z,
                                           int surfaceY, int anchorY, double distance, TerrainBlendConfig config,
                                           ColumnMaterial material) {
        if (config.slopeHeight() <= 0 || anchorY <= surfaceY + 1) return 0;

        double shoulderFactor = 1.0 - (distance / Math.max(1, config.blendRadius()));
        int shoulderHeight = (int) Math.ceil(config.slopeHeight() * smoothStep(shoulderFactor));
        int targetY = Math.min(anchorY - 1, surfaceY + shoulderHeight);
        int changed = 0;
        for (int y = surfaceY + 1; y <= targetY; y++) {
            pos.set(x, y, z);
            BlockState current = level.getBlockState(pos);
            if (isFillBlocked(current)) break;

            BlockState state = y == targetY ? shoulderTop(material) : material.filler();
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            changed += clearUnsupportedPlant(level, pos, x, y + 1, z);
            changed++;
        }
        return changed;
    }

    private static int foundationTargetY(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                                         int x, int z, int anchorY, int surfaceY, TerrainBlendConfig config) {
        int maxDrop = config.maxAllowedSurfaceDrop();
        int bottomY = Math.max(level.getMinBuildHeight(), anchorY - maxDrop);
        for (int y = anchorY - 1; y >= bottomY; y--) {
            pos.set(x, y, z);
            if (isStableFoundation(level.getBlockState(pos))) {
                return y + 1;
            }
        }
        return Math.max(surfaceY, bottomY);
    }

    private static boolean isFillBlocked(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && !isLoosePlant(state);
    }

    private static boolean isFoundationBlocked(BlockState state) {
        return isFillBlocked(state) && !isNaturalSurface(state);
    }

    private static boolean isStableFoundation(BlockState state) {
        return isFoundationBlocked(state);
    }

    private static BlockState shoulderTop(ColumnMaterial material) {
        return material.top().is(Blocks.GRASS_BLOCK)
                || material.top().is(Blocks.DIRT)
                || material.top().is(Blocks.COARSE_DIRT)
                || material.top().is(Blocks.ROOTED_DIRT)
                || material.filler().is(Blocks.DIRT)
                || material.filler().is(Blocks.COARSE_DIRT)
                || material.filler().is(Blocks.ROOTED_DIRT)
                ? Blocks.GRASS_BLOCK.defaultBlockState()
                : material.top();
    }

    private static double smoothStep(double value) {
        double clamped = Math.max(0.0, Math.min(1.0, value));
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    private static int clearUnsupportedPlant(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z) {
        int changed = 0;
        int maxY = Math.min(level.getMaxBuildHeight() - 1, y + 2);
        for (int plantY = y; plantY <= maxY; plantY++) {
            pos.set(x, plantY, z);
            BlockState state = level.getBlockState(pos);
            if (!isLoosePlant(state) && !isFlower(state)) break;

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            changed++;
        }
        return changed;
    }

    private static boolean isLoosePlant(BlockState state) {
        return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS);
    }

    private static boolean isFlower(BlockState state) {
        return state.is(Blocks.DANDELION)
                || state.is(Blocks.POPPY)
                || state.is(Blocks.BLUE_ORCHID)
                || state.is(Blocks.ALLIUM)
                || state.is(Blocks.AZURE_BLUET)
                || state.is(Blocks.RED_TULIP)
                || state.is(Blocks.ORANGE_TULIP)
                || state.is(Blocks.WHITE_TULIP)
                || state.is(Blocks.PINK_TULIP)
                || state.is(Blocks.OXEYE_DAISY)
                || state.is(Blocks.CORNFLOWER)
                || state.is(Blocks.LILY_OF_THE_VALLEY)
                || state.is(Blocks.TORCHFLOWER)
                || state.is(Blocks.WITHER_ROSE);
    }

    private static boolean isFootprintBlock(BlockState state) {
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && !isLoosePlant(state)
                && !isFlower(state)
                && !state.is(Blocks.TORCH)
                && !state.is(Blocks.WALL_TORCH)
                && !state.is(Blocks.LANTERN)
                && !state.is(Blocks.CHAIN)
                && !state.is(Blocks.VINE)
                && !state.is(Blocks.LADDER)
                && !state.is(Blocks.SNOW)
                && !state.is(Blocks.JIGSAW)
                && !state.is(Blocks.STRUCTURE_BLOCK)
                && !state.is(Blocks.STRUCTURE_VOID);
    }

    private static boolean isNaturalSurface(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.MOSS_BLOCK);
    }

    private static boolean isBlendMaterial(BlockState state) {
        return isNaturalSurface(state)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.STONE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD);
    }

    private static BlockState defaultFiller(BlockState top) {
        if (top.is(Blocks.GRASS_BLOCK) || top.is(Blocks.PODZOL) || top.is(Blocks.MYCELIUM)
                || top.is(Blocks.MOSS_BLOCK)) {
            return Blocks.DIRT.defaultBlockState();
        }
        return top;
    }

    private record AnchorDistance(BlockPos anchor, int distanceSq) {
    }

    private record ColumnMaterial(BlockState top, BlockState filler) {
    }
}
