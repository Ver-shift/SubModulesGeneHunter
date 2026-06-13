package org.galaxy.gene_hunter.api.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.galaxy.gene_hunter.GeneHunter;

/**
 * GeneHunter 标签定义
 */
public class GeneHunterTags {


    public static final TagKey<Item> ONE_HAND_WEAPON = createItemTag("one_hand_weapon");
    public static final TagKey<Item> TWO_HAND_WEAPON = createItemTag("two_hand_weapon");
    public static final TagKey<Item> POLEARM_WEAPON = createItemTag("polearm_weapon");

    public static final TagKey<Item> BLADE_WEAPON = createItemTag("blade_weapon");
    public static final TagKey<Item> SWORD_WEAPON = createItemTag("sword_weapon");
    public static final TagKey<Item> HALBERD_WEAPON = createItemTag("halberd_weapon");
    public static final TagKey<Item> AXE_WEAPON = createItemTag("axe_weapon");
    public static final TagKey<Item> HAMMER_WEAPON = createItemTag("hammer_weapon");


    private static TagKey<Item> createItemTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(GeneHunter.MODID, name));
    }

    private static TagKey<Block> createBlockTag(String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(GeneHunter.MODID, name));
    }
}
