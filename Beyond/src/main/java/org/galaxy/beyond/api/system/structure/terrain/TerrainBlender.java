package org.galaxy.beyond.api.system.structure.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class TerrainBlender {

    private static final Block[] IGNORED_TEMPLATE_BLOCKS = {
            Blocks.AIR,
            Blocks.CAVE_AIR,
            Blocks.VOID_AIR,
            Blocks.STRUCTURE_BLOCK,
            Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW
    };
    private static final double SLOPE_NOISE_SCALE = 6.0;
    private static final int SLOPE_NOISE_STRENGTH = 3;
    /**
     * A structure is placed once per intersecting chunk. Keep its immutable template footprint
     * around so those chunk callbacks do not repeatedly decode the same template palettes.
     */
    private static final Map<PiecesContainer, Map<Integer, List<BlockPos>>> TEMPLATE_ANCHOR_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    private TerrainBlender() {
    }

    public static void blend(WorldGenLevel level, BoundingBox placeBox, ChunkPos chunkPos,
                             PiecesContainer pieces, TerrainBlendConfig config) {
        long startedAt = System.nanoTime();
        BoundingBox chunkBox = new BoundingBox(
                chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ()
        );
        BoundingBox structureBox = pieces.calculateBoundingBox();
        BoundingBox targetBox = intersect(chunkBox, placeBox);
        if (targetBox == null) return;

        int blendRadius = config.blendRadius();
        BoundingBox anchorSearchBox = new BoundingBox(
                structureBox.minX() - blendRadius, targetBox.minY(), structureBox.minZ() - blendRadius,
                structureBox.maxX() + blendRadius, targetBox.maxY(), structureBox.maxZ() + blendRadius
        );
        List<BlockPos> anchors = collectAnchors(level, pieces, config.depth(), anchorSearchBox);
        if (anchors.isEmpty()) return;

        Footprint footprint = Footprint.create(anchors);
        int minX = Math.max(targetBox.minX(), footprint.bounds().minX() - blendRadius);
        int maxX = Math.min(targetBox.maxX(), footprint.bounds().maxX() + blendRadius);
        int minZ = Math.max(targetBox.minZ(), footprint.bounds().minZ() - blendRadius);
        int maxZ = Math.min(targetBox.maxZ(), footprint.bounds().maxZ() + blendRadius);
        if (minX > maxX || minZ > maxZ) return;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int blendRadiusSq = blendRadius * blendRadius;
        int blendedColumns = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                AnchorDistance nearest = footprint.nearest(x, z, blendRadiusSq);
                if (nearest.distanceSq() > blendRadiusSq) continue;

                int anchorY = nearest.anchor().getY();
                boolean anchorColumn = nearest.distanceSq() == 0;
                int maxSurfaceY = anchorColumn ? anchorY - 1 : level.getMaxBuildHeight() - 1;
                TerrainColumn terrain = sampleTerrainColumn(level, mutable, x, z, maxSurfaceY);
                if (terrain.surfaceY() <= level.getMinBuildHeight()) continue;

                BlendColumn column = BlendColumn.create(nearest, x, z, terrain.surfaceY(), config);
                blendColumn(level, mutable, x, z, column, config, terrain.material());
                blendedColumns++;
            }
        }
        Beyond.debugInfo("Terrain blend chunk=[{}, {}] columns={} elapsed={}us",
                chunkPos.x, chunkPos.z, blendedColumns, (System.nanoTime() - startedAt) / 1_000L);
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

    private static List<BlockPos> collectAnchors(WorldGenLevel level, PiecesContainer pieces,
                                                 int depth, BoundingBox searchBox) {
        List<BlockPos> anchors = filterAnchors(templateFootprintAnchors(level, pieces, depth), searchBox);
        if (!anchors.isEmpty()) {
            return anchors;
        }
        return collectPlacedFootprintAnchors(level, pieces, depth, searchBox);
    }

    private static List<BlockPos> templateFootprintAnchors(WorldGenLevel level, PiecesContainer pieces, int depth) {
        synchronized (TEMPLATE_ANCHOR_CACHE) {
            Map<Integer, List<BlockPos>> anchorsByDepth = TEMPLATE_ANCHOR_CACHE.computeIfAbsent(
                    pieces, ignored -> new HashMap<>());
            return anchorsByDepth.computeIfAbsent(depth,
                    ignored -> List.copyOf(collectTemplateFootprintAnchors(level, pieces, depth)));
        }
    }

    private static List<BlockPos> collectTemplateFootprintAnchors(WorldGenLevel level, PiecesContainer pieces,
                                                                   int depth) {
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
                addLowestAnchor(anchors, anchor);
            }
        }
        return new ArrayList<>(anchors.values());
    }

    private static List<BlockPos> filterAnchors(List<BlockPos> anchors, BoundingBox searchBox) {
        List<BlockPos> filtered = new ArrayList<>();
        for (BlockPos anchor : anchors) {
            if (searchBox.isInside(anchor)) {
                filtered.add(anchor);
            }
        }
        return filtered;
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

    /**
     * Finds the visible terrain surface and its reusable top/filler materials in one descent.
     * Previously these were two separate scans over the same world column.
     */
    private static TerrainColumn sampleTerrainColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                                                     int x, int z, int maxY) {
        int minY = level.getMinBuildHeight();
        int scanStartY = Math.min(level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1, maxY);
        int surfaceY = minY;
        boolean foundSurface = false;
        BlockState top = null;
        BlockState filler = null;

        for (int y = scanStartY; y >= minY; y--) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!foundSurface) {
                if (state.isAir() || isLoosePlant(state)) continue;
                surfaceY = y;
                foundSurface = true;
            }
            if (!isBlendMaterial(state)) continue;

            if (top == null) {
                top = state;
            } else {
                filler = state;
                break;
            }
        }

        if (!foundSurface) {
            return new TerrainColumn(minY, null);
        }
        if (top == null) {
            top = Blocks.DIRT.defaultBlockState();
        }
        if (filler == null) {
            filler = defaultFiller(top);
        }
        return new TerrainColumn(surfaceY, new ColumnMaterial(top, filler));
    }

    private static void blendColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z,
                                    BlendColumn column, TerrainBlendConfig config, ColumnMaterial material) {
        if (column.targetY() > column.surfaceY()) {
            fillColumnUp(level, pos, x, z, column.surfaceY() + 1, column.targetY(), material);
        } else if (column.targetY() < column.surfaceY()) {
            carveColumnDown(level, pos, x, z, column.surfaceY(), column.targetY(), material);
        } else {
            refreshColumnTop(level, pos, x, column.targetY(), z, material);
        }

        if (column.anchorColumn()) {
            int bottomY = Math.max(level.getMinBuildHeight(), column.anchorY() - config.foundationDepth());
            fillFoundation(level, pos, x, z, column.anchorY() - 1, bottomY, material);
        }
    }

    private static void fillColumnUp(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z,
                                     int startY, int targetY, ColumnMaterial material) {
        for (int y = startY; y <= targetY; y++) {
            pos.set(x, y, z);
            BlockState current = level.getBlockState(pos);
            if (!canReplaceForBlend(current)) return;

            level.setBlock(pos, y == targetY ? blendTop(material) : blendFiller(material), Block.UPDATE_CLIENTS);
            clearUnsupportedPlant(level, pos, x, y + 1, z);
        }
    }

    private static void carveColumnDown(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z,
                                        int surfaceY, int targetY, ColumnMaterial material) {
        for (int y = surfaceY; y > targetY; y--) {
            pos.set(x, y, z);
            BlockState current = level.getBlockState(pos);
            if (!canCarveForBlend(current)) return;

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            clearUnsupportedPlant(level, pos, x, y + 1, z);
        }
        refreshColumnTop(level, pos, x, targetY, z, material);
    }

    private static void refreshColumnTop(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z,
                                         ColumnMaterial material) {
        pos.set(x, y, z);
        BlockState current = level.getBlockState(pos);
        if (canReplaceForBlend(current) || isBlendMaterial(current)) {
            level.setBlock(pos, blendTop(material), Block.UPDATE_CLIENTS);
        }
    }

    private static void fillFoundation(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                                       int x, int z, int startY, int bottomY, ColumnMaterial material) {
        for (int y = Math.min(startY, level.getMaxBuildHeight() - 1); y >= bottomY; y--) {
            pos.set(x, y, z);
            BlockState current = level.getBlockState(pos);
            if (!canReplaceForBlend(current) && !isNaturalSurface(current)) return;

            level.setBlock(pos, blendFiller(material), Block.UPDATE_CLIENTS);
            clearUnsupportedPlant(level, pos, x, y + 1, z);
        }
    }

    private static boolean canReplaceForBlend(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty() || isLoosePlant(state) || isFlower(state);
    }

    private static boolean canCarveForBlend(BlockState state) {
        return canReplaceForBlend(state) || isBlendMaterial(state);
    }

    private static BlockState blendTop(ColumnMaterial material) {
        if (isSoilLike(material.top()) || isRockLike(material.top()) || isSoilLike(material.filler()) || isRockLike(material.filler())) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
        return material.top();
    }

    private static BlockState blendFiller(ColumnMaterial material) {
        return isSoilLike(material.filler()) || isRockLike(material.filler())
                ? Blocks.DIRT.defaultBlockState()
                : material.filler();
    }

    private static boolean isSoilLike(BlockState state) {
        return isNaturalSurface(state)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.ROOTED_DIRT);
    }

    private static boolean isRockLike(BlockState state) {
        return state.is(Blocks.STONE)
                || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.TUFF)
                || state.is(Blocks.DEEPSLATE);
    }

    private static double smoothStep(double value) {
        double clamped = clampUnit(value);
        return clamped * clamped * (3.0 - 2.0 * clamped);
    }

    private static double clampUnit(double value) {
        if (value < 0.0) return 0.0;
        return Math.min(value, 1.0);
    }

    private static int clampNonNegative(int value, int max) {
        if (value < 0) return 0;
        return Math.min(value, max);
    }

    private static int applySlopeNoise(int x, int z, int targetY, int surfaceY, double distance,
                                       int innerRadius, TerrainBlendConfig config) {
        double progress = clampUnit((distance - innerRadius) / Math.max(1.0, config.blendRadius() - innerRadius));
        double mask = Math.sin(progress * Math.PI);
        if (mask <= 0.0) return targetY;

        int offset = (int) Math.round(columnNoise(x, z) * SLOPE_NOISE_STRENGTH * mask);
        return Math.min(targetY + offset, surfaceY + config.slopeHeight());
    }

    private static double columnNoise(int x, int z) {
        double sampleX = x / SLOPE_NOISE_SCALE;
        double sampleZ = z / SLOPE_NOISE_SCALE;
        int minX = (int) Math.floor(sampleX);
        int minZ = (int) Math.floor(sampleZ);
        double localX = sampleX - minX;
        double localZ = sampleZ - minZ;
        double smoothX = smoothStep(localX);
        double smoothZ = smoothStep(localZ);

        double north = lerp(noiseAt(minX, minZ), noiseAt(minX + 1, minZ), smoothX);
        double south = lerp(noiseAt(minX, minZ + 1), noiseAt(minX + 1, minZ + 1), smoothX);
        return lerp(north, south, smoothZ) * 2.0 - 1.0;
    }

    private static double noiseAt(int x, int z) {
        long value = x * 341873128712L + z * 132897987541L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return (value & 0xFFFFFFL) / (double) 0x1000000L;
    }

    private static double lerp(double from, double to, double factor) {
        return from + (to - from) * factor;
    }

    private static void clearUnsupportedPlant(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z) {
        int maxY = Math.min(level.getMaxBuildHeight() - 1, y + 2);
        for (int plantY = y; plantY <= maxY; plantY++) {
            pos.set(x, plantY, z);
            BlockState state = level.getBlockState(pos);
            if (!isLoosePlant(state) && !isFlower(state)) break;

            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
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
        return !isIgnoredTemplateBlock(state)
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

    private static boolean isIgnoredTemplateBlock(BlockState state) {
        for (Block block : IGNORED_TEMPLATE_BLOCKS) {
            if (state.is(block)) return true;
        }
        return state.isAir();
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

    private record TerrainColumn(int surfaceY, ColumnMaterial material) {
    }

    private record BlendColumn(int anchorY, int surfaceY, int targetY, boolean anchorColumn) {

        private static BlendColumn create(AnchorDistance nearest, int x, int z, int surfaceY,
                                          TerrainBlendConfig config) {
            int anchorY = nearest.anchor().getY();
            double distance = Math.sqrt(nearest.distanceSq());
            int innerRadius = clampNonNegative(config.flatnessRadius(), config.blendRadius() - 1);
            double weight = blendWeight(distance, innerRadius, config.blendRadius());
            int targetY = (int) Math.round(lerp(surfaceY, anchorY - 1, weight));
            if (nearest.distanceSq() != 0) {
                targetY = applySlopeNoise(x, z, targetY, surfaceY, distance, innerRadius, config);
            }
            return new BlendColumn(anchorY, surfaceY, targetY, nearest.distanceSq() == 0);
        }

        private static double blendWeight(double distance, int innerRadius, int blendRadius) {
            if (distance <= innerRadius) return 1.0;
            double outer = Math.max(1.0, blendRadius - innerRadius);
            return 1.0 - smoothStep((distance - innerRadius) / outer);
        }
    }

    private record Footprint(List<BlockPos> anchors, BoundingBox bounds) {

        private static Footprint create(List<BlockPos> anchors) {
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (BlockPos anchor : anchors) {
                minX = Math.min(minX, anchor.getX());
                minY = Math.min(minY, anchor.getY());
                minZ = Math.min(minZ, anchor.getZ());
                maxX = Math.max(maxX, anchor.getX());
                maxY = Math.max(maxY, anchor.getY());
                maxZ = Math.max(maxZ, anchor.getZ());
            }
            return new Footprint(anchors, new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
        }

        private AnchorDistance nearest(int x, int z, int cutoffSq) {
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
    }
}
