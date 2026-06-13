package org.galaxy.gene_hunter.datagen.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterItemInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;
import org.galaxy.gene_hunter.api.system.weapon.SimplySwordsWeaponClasses.Family;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent.WeaponGrip;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * GeneHunter 物品标签提供者
 * 用于为物品添加各种标签
 */
public class GeneHunterTagProvider extends ItemTagsProvider {
    private static final Item[] VANILLA_AXES = {
            Items.WOODEN_AXE,
            Items.STONE_AXE,
            Items.IRON_AXE,
            Items.GOLDEN_AXE,
            Items.DIAMOND_AXE,
            Items.NETHERITE_AXE
    };
    private static final Item[] VANILLA_SWORDS = {
            Items.WOODEN_SWORD,
            Items.STONE_SWORD,
            Items.IRON_SWORD,
            Items.GOLDEN_SWORD,
            Items.DIAMOND_SWORD,
            Items.NETHERITE_SWORD
    };

    public GeneHunterTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, GeneHunter.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        // ========== 单手武器 ==========
        tagGrip(GeneHunterTags.ONE_HAND_WEAPON, WeaponGrip.ONE_HAND);
        tag(GeneHunterTags.ONE_HAND_WEAPON).add(GeneHunterItemInit.TEST_SINGLE_HAND_SWORD.get());
        tag(GeneHunterTags.ONE_HAND_WEAPON).add(GeneHunterItemInit.TEST_ONE_HAND_DAMAGE_SWORD.get());
        tagItems(GeneHunterTags.ONE_HAND_WEAPON, VANILLA_AXES);
        tagItems(GeneHunterTags.ONE_HAND_WEAPON, VANILLA_SWORDS);

        // ========== 双手武器 ==========
        tagGrip(GeneHunterTags.TWO_HAND_WEAPON, WeaponGrip.TWO_HAND);

        // ========== 长杆武器 ==========
        tagGrip(GeneHunterTags.POLEARM_WEAPON, WeaponGrip.POLEARM);

        // ========== 武器形态 ==========
        tagFamilies(GeneHunterTags.BLADE_WEAPON, Family.SAI, Family.CUTLASS, Family.KATANA,
                Family.CHAKRAM);
        tagFamilies(GeneHunterTags.SWORD_WEAPON, Family.RAPIER, Family.LONGSWORD, Family.CLAYMORE,
                Family.TWINBLADE);
        tag(GeneHunterTags.SWORD_WEAPON).add(GeneHunterItemInit.TEST_SINGLE_HAND_SWORD.get());
        tag(GeneHunterTags.SWORD_WEAPON).add(GeneHunterItemInit.TEST_ONE_HAND_DAMAGE_SWORD.get());
        tagItems(GeneHunterTags.SWORD_WEAPON, VANILLA_SWORDS);
        tagFamilies(GeneHunterTags.HALBERD_WEAPON, Family.SPEAR, Family.HALBERD, Family.GLAIVE,
                Family.SCYTHE);
        tagFamilies(GeneHunterTags.AXE_WEAPON, Family.GREATAXE, Family.WARGLAIVE);
        tagItems(GeneHunterTags.AXE_WEAPON, VANILLA_AXES);
        tagFamilies(GeneHunterTags.HAMMER_WEAPON, Family.GREATHAMMER);
    }

    private void tagGrip(TagKey<Item> tagKey, WeaponGrip grip) {
        addFamilies(tag(tagKey), family -> family.grip() == grip);
    }

    private void tagFamilies(TagKey<Item> tagKey, Family... families) {
        IntrinsicTagAppender<Item> tag = tag(tagKey);
        for (Family family : families) {
            family.items().forEach(item -> tag.add(item.get()));
        }
    }

    private void tagItems(TagKey<Item> tagKey, Item... items) {
        tag(tagKey).add(items);
    }

    private void addFamilies(IntrinsicTagAppender<Item> tag, java.util.function.Predicate<Family> predicate) {
        for (Family family : Family.values()) {
            if (!predicate.test(family)) {
                continue;
            }
            family.items().forEach(item -> tag.add(item.get()));
        }
    }

}
