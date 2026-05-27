package org.galaxy.beyond.api.datagen.custom;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.galaxy.beyond.Beyond;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BeyondWorldgenProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.TEMPLATE_POOL, BeyondWorldgenProvider::bootstrapTemplatePools)
            .add(Registries.STRUCTURE, BeyondWorldgenProvider::bootstrapStructures)
            .add(Registries.STRUCTURE_SET, BeyondWorldgenProvider::bootstrapStructureSets);

    private BeyondWorldgenProvider() {
    }

    private static void bootstrapTemplatePools(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);
        var emptyPool = pools.getOrThrow(Pools.EMPTY);
        var emptyProcessors = net.minecraft.core.Holder.direct(new StructureProcessorList(List.of()));

        context.register(BeyondWorldgenKeys.SAFE_STRUCTURE_POOL, singlePool(emptyPool, emptyProcessors, "safe_structure"));
        context.register(BeyondWorldgenKeys.PILLAGER_CAMP_POOL, singlePool(emptyPool, emptyProcessors, "pillager_camp"));
    }



    private static StructureTemplatePool singlePool(net.minecraft.core.Holder<StructureTemplatePool> fallback,
                                                    net.minecraft.core.Holder<StructureProcessorList> processors,
                                                    String structureName) {
        return new StructureTemplatePool(
                fallback,
                List.of(Pair.of(StructurePoolElement.single(Beyond.MODID + ":" + structureName, processors), 1)),
                StructureTemplatePool.Projection.RIGID
        );
    }

    private static void bootstrapStructures(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(BeyondWorldgenKeys.SAFE_STRUCTURE, jigsaw(
                new Structure.StructureSettings(
                        HolderSet.direct(biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS)),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_BOX
                ),
                pools.getOrThrow(BeyondWorldgenKeys.SAFE_STRUCTURE_POOL)
        ));

        context.register(BeyondWorldgenKeys.PILLAGER_CAMP, jigsaw(
                new Structure.StructureSettings(
                        biomes.getOrThrow(BeyondWorldgenKeys.HAS_PILLAGER_CAMP),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_BOX
                ),
                pools.getOrThrow(BeyondWorldgenKeys.PILLAGER_CAMP_POOL)
        ));
    }

    private static JigsawStructure jigsaw(Structure.StructureSettings settings,
                                          net.minecraft.core.Holder<StructureTemplatePool> startPool) {
        return new JigsawStructure(
                settings,
                startPool,
                Optional.empty(),
                1,
                ConstantHeight.of(VerticalAnchor.absolute(0)),
                false,
                Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                116,
                List.of(),
                JigsawStructure.DEFAULT_DIMENSION_PADDING,
                JigsawStructure.DEFAULT_LIQUID_SETTINGS
        );
    }

    private static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        context.register(BeyondWorldgenKeys.SAFE_STRUCTURE_SET, new StructureSet(
                List.of(
                        StructureSet.entry(structures.getOrThrow(BeyondWorldgenKeys.SAFE_STRUCTURE), 1),
                        StructureSet.entry(structures.getOrThrow(BeyondWorldgenKeys.PILLAGER_CAMP), 20)
                ),
                new RandomSpreadStructurePlacement(30, 16, RandomSpreadType.LINEAR, 294765103)
        ));
    }
}
