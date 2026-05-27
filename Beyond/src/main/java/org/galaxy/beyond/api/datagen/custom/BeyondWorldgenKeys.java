package org.galaxy.beyond.api.datagen.custom;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.galaxy.beyond.Beyond;

public final class BeyondWorldgenKeys {
    public static final ResourceKey<StructureTemplatePool> SAFE_STRUCTURE_POOL =
            ResourceKey.create(Registries.TEMPLATE_POOL, Beyond.asResource("safe_structure_pool"));
    public static final ResourceKey<StructureTemplatePool> PILLAGER_CAMP_POOL =
            ResourceKey.create(Registries.TEMPLATE_POOL, Beyond.asResource("pillager_camp_pool"));

    public static final ResourceKey<Structure> SAFE_STRUCTURE =
            ResourceKey.create(Registries.STRUCTURE, Beyond.asResource("safe_structure"));
    public static final ResourceKey<Structure> PILLAGER_CAMP =
            ResourceKey.create(Registries.STRUCTURE, Beyond.asResource("pillager_camp"));

    public static final ResourceKey<StructureSet> SAFE_STRUCTURE_SET =
            ResourceKey.create(Registries.STRUCTURE_SET, Beyond.asResource("safe_structure_set"));

    public static final TagKey<Biome> HAS_PILLAGER_CAMP =
            TagKey.create(Registries.BIOME, Beyond.asResource("has_pillager_camp"));
    public static final TagKey<Structure> SAFE_ZONE_STRUCTURE =
            TagKey.create(Registries.STRUCTURE, Beyond.asResource("safe_zone_structure"));
    public static final TagKey<Structure> NODE_STRUCTURE =
            TagKey.create(Registries.STRUCTURE, Beyond.asResource("node_structure"));

    private BeyondWorldgenKeys() {
    }
}
