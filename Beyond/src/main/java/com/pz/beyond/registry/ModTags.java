package com.pz.beyond.registry;

import com.pz.beyond.Beyond;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ModTags {
    public static final TagKey<EntityType<?>> NO_SAFE_ZONE_SPAWN = tag("no_safe_zone_spawn");


    private static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Beyond.MODID, name));
    }
}
