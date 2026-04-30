package com.pz.beyond.api.tag;

import com.pz.beyond.Beyond;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Beyond 模组自定义的实体类型 Tag 常量集合。
 * <p>
 * 通过 DataGen 生成数据文件到 {@code data/beyond/tags/entity_type/*.json}。
 */
public final class BeyondEntityTags {

    private BeyondEntityTags() {
    }

    /**
     * 敌对生物（所有实现 {@link net.minecraft.world.entity.monster.Enemy} 的原版实体）。
     * 数据包作者与其他模组可追加条目。
     */
    public static final TagKey<EntityType<?>> HOSTILE = create("hostile");

    private static TagKey<EntityType<?>> create(String path) {
        return TagKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Beyond.MODID, path));
    }
}
