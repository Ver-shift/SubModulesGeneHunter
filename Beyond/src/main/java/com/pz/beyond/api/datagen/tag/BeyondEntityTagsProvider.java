package com.pz.beyond.api.datagen.tag;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.tag.BeyondEntityTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Beyond 实体类型 Tag 生成器。
 * <p>
 * 敌对生物列表 = 原版所有实现 {@link net.minecraft.world.entity.monster.Enemy} 接口的 EntityType，
 * 包括 {@code Monster} 子类、{@code Slime}/{@code MagmaCube}、{@code Ghast}、{@code Phantom}、
 * {@code Shulker}、{@code EnderDragon}、{@code WitherBoss} 以及 NeutralMob 中实现了 Enemy 的
 * 僵尸猪灵等，覆盖 1.21 原版全部敌对 EntityType。
 */
public class BeyondEntityTagsProvider extends EntityTypeTagsProvider {

    public BeyondEntityTagsProvider(PackOutput output,
                                    CompletableFuture<HolderLookup.Provider> lookup,
                                    ExistingFileHelper helper) {
        super(output, lookup, Beyond.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BeyondEntityTags.HOSTILE)
                // Monster 子类 —— 陆地敌对
                .add(EntityType.BLAZE)
                .add(EntityType.BOGGED)
                .add(EntityType.BREEZE)
                .add(EntityType.CAVE_SPIDER)
                .add(EntityType.CREEPER)
                .add(EntityType.DROWNED)
                .add(EntityType.ELDER_GUARDIAN)
                .add(EntityType.ENDERMAN)
                .add(EntityType.ENDERMITE)
                .add(EntityType.EVOKER)
                .add(EntityType.GIANT)
                .add(EntityType.GUARDIAN)
                .add(EntityType.HOGLIN)
                .add(EntityType.HUSK)
                .add(EntityType.ILLUSIONER)
                .add(EntityType.PIGLIN)
                .add(EntityType.PIGLIN_BRUTE)
                .add(EntityType.PILLAGER)
                .add(EntityType.RAVAGER)
                .add(EntityType.SILVERFISH)
                .add(EntityType.SKELETON)
                .add(EntityType.SPIDER)
                .add(EntityType.STRAY)
                .add(EntityType.VEX)
                .add(EntityType.VINDICATOR)
                .add(EntityType.WARDEN)
                .add(EntityType.WITCH)
                .add(EntityType.WITHER_SKELETON)
                .add(EntityType.ZOGLIN)
                .add(EntityType.ZOMBIE)
                .add(EntityType.ZOMBIE_VILLAGER)
                .add(EntityType.ZOMBIFIED_PIGLIN)
                // 非 Monster 但实现 Enemy
                .add(EntityType.GHAST)
                .add(EntityType.SLIME)
                .add(EntityType.MAGMA_CUBE)
                .add(EntityType.PHANTOM)
                .add(EntityType.SHULKER)
                // Boss
                .add(EntityType.ENDER_DRAGON)
                .add(EntityType.WITHER);
    }
}
