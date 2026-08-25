package org.galaxy.gene_hunter.datagen.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterItemInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;
import org.galaxy.gene_hunter.api.system.weapon.SimplySwordsWeaponClasses.Family;
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
        // ========== 武器形态 ==========
        tagFamilies(GeneHunterTags.BLADE_WEAPON, Family.SAI, Family.CUTLASS, Family.KATANA,
                Family.CHAKRAM, Family.UNIQUE_BLADE);
        tagFamilies(GeneHunterTags.SWORD_WEAPON, Family.RAPIER, Family.LONGSWORD, Family.CLAYMORE,
                Family.TWINBLADE, Family.SPEAR, Family.HALBERD, Family.GLAIVE, Family.UNIQUE_SWORD);
        // Present only in some Simply Swords configurations/versions.
        tag(GeneHunterTags.SWORD_WEAPON).addOptional(ResourceLocation.fromNamespaceAndPath("simplyswords", "dreadtide"));
        tag(GeneHunterTags.SWORD_WEAPON).add(GeneHunterItemInit.TEST_SINGLE_HAND_SWORD.get());
        tag(GeneHunterTags.SWORD_WEAPON).add(GeneHunterItemInit.TEST_ONE_HAND_DAMAGE_SWORD.get());
        tagItems(GeneHunterTags.SWORD_WEAPON, VANILLA_SWORDS);
        tagFamilies(GeneHunterTags.AXE_WEAPON, Family.GREATAXE, Family.WARGLAIVE, Family.SCYTHE, Family.UNIQUE_AXE);
        tagItems(GeneHunterTags.AXE_WEAPON, VANILLA_AXES);
        tagFamilies(GeneHunterTags.HAMMER_WEAPON, Family.GREATHAMMER, Family.UNIQUE_HAMMER);
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

}
