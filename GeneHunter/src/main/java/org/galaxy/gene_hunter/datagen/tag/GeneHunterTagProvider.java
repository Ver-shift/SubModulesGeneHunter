package org.galaxy.gene_hunter.datagen.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;
import org.galaxy.gene_hunter.api.system.weapon.SimplySwordsWeaponClasses;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent.WeaponGrip;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent.WeaponShape;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * GeneHunter 物品标签提供者
 * 用于为物品添加各种标签
 */
public class GeneHunterTagProvider extends ItemTagsProvider {

    public GeneHunterTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, GeneHunter.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // ========== 单手武器 ==========
        tagGrip(GeneHunterTags.ONE_HAND_WEAPON, WeaponGrip.ONE_HAND);

        // ========== 双手武器 ==========
        tagGrip(GeneHunterTags.TWO_HAND_WEAPON, WeaponGrip.TWO_HAND);

        // ========== 长杆武器 ==========
        tagGrip(GeneHunterTags.POLEARM_WEAPON, WeaponGrip.POLEARM);

        // ========== 武器形态 ==========
        tagShape(GeneHunterTags.BLADE_WEAPON, WeaponShape.BLADE);
        tagShape(GeneHunterTags.SWORD_WEAPON, WeaponShape.SWORD);
        tagShape(GeneHunterTags.HALBERD_WEAPON, WeaponShape.HALBERD);
        tagShape(GeneHunterTags.AXE_WEAPON, WeaponShape.AXE);
        tagShape(GeneHunterTags.HAMMER_WEAPON, WeaponShape.HAMMER);
    }

    private void tagGrip(TagKey<Item> tagKey, WeaponGrip grip) {
        addFamilies(tag(tagKey), family -> family.grip() == grip);
    }

    private void tagShape(TagKey<Item> tagKey, WeaponShape shape) {
        addFamilies(tag(tagKey), family -> family.shape() == shape);
    }

    private void addFamilies(IntrinsicTagAppender<Item> tag, java.util.function.Predicate<SimplySwordsWeaponClasses.Family> predicate) {
        for (SimplySwordsWeaponClasses.Family family : SimplySwordsWeaponClasses.Family.values()) {
            if (!predicate.test(family)) {
                continue;
            }
            family.items().forEach(item -> tag.add(item.get()));
        }
    }

}
