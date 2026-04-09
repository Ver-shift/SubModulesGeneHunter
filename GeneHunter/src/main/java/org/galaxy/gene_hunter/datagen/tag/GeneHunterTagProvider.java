package org.galaxy.gene_hunter.datagen.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.sweenus.simplyswords.SimplySwords;
import net.sweenus.simplyswords.registry.ItemsRegistry;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;
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
        tag(GeneHunterTags.ONE_HAND_WEAPON)
            // 刺剑
            .add(ItemsRegistry.IRON_RAPIER.get())
            .add(ItemsRegistry.GOLD_RAPIER.get())
            .add(ItemsRegistry.DIAMOND_RAPIER.get())
            .add(ItemsRegistry.NETHERITE_RAPIER.get())
            .add(ItemsRegistry.RUNIC_RAPIER.get())
            // 环刃
            .add(ItemsRegistry.IRON_CHAKRAM.get())
            .add(ItemsRegistry.GOLD_CHAKRAM.get())
            .add(ItemsRegistry.DIAMOND_CHAKRAM.get())
            .add(ItemsRegistry.NETHERITE_CHAKRAM.get())
            .add(ItemsRegistry.RUNIC_CHAKRAM.get())
            // 短剑
            .add(ItemsRegistry.IRON_SAI.get())
            .add(ItemsRegistry.GOLD_SAI.get())
            .add(ItemsRegistry.DIAMOND_SAI.get())
            .add(ItemsRegistry.NETHERITE_SAI.get())
            .add(ItemsRegistry.RUNIC_SAI.get())
            // 弯刀
            .add(ItemsRegistry.IRON_CUTLASS.get())
            .add(ItemsRegistry.GOLD_CUTLASS.get())
            .add(ItemsRegistry.DIAMOND_CUTLASS.get())
            .add(ItemsRegistry.NETHERITE_CUTLASS.get())
            .add(ItemsRegistry.RUNIC_CUTLASS.get())
            // 双持剑
            .add(ItemsRegistry.IRON_TWINBLADE.get())
            .add(ItemsRegistry.GOLD_TWINBLADE.get())
            .add(ItemsRegistry.DIAMOND_TWINBLADE.get())
            .add(ItemsRegistry.NETHERITE_TWINBLADE.get())
            .add(ItemsRegistry.RUNIC_TWINBLADE.get())
            // 战刃
            .add(ItemsRegistry.IRON_WARGLAIVE.get())
            .add(ItemsRegistry.GOLD_WARGLAIVE.get())
            .add(ItemsRegistry.DIAMOND_WARGLAIVE.get())
            .add(ItemsRegistry.NETHERITE_WARGLAIVE.get())
            .add(ItemsRegistry.RUNIC_WARGLAIVE.get())
            // 长刀
            .add(ItemsRegistry.IRON_GLAIVE.get())
            .add(ItemsRegistry.GOLD_GLAIVE.get())
            .add(ItemsRegistry.DIAMOND_GLAIVE.get())
            .add(ItemsRegistry.NETHERITE_GLAIVE.get())
            .add(ItemsRegistry.RUNIC_GLAIVE.get())
            // 武士刀
            .add(ItemsRegistry.IRON_KATANA.get())
            .add(ItemsRegistry.GOLD_KATANA.get())
            .add(ItemsRegistry.DIAMOND_KATANA.get())
            .add(ItemsRegistry.NETHERITE_KATANA.get())
            .add(ItemsRegistry.RUNIC_KATANA.get())
            // 长剑
            .add(ItemsRegistry.IRON_LONGSWORD.get())
            .add(ItemsRegistry.GOLD_LONGSWORD.get())
            .add(ItemsRegistry.DIAMOND_LONGSWORD.get())
            .add(ItemsRegistry.NETHERITE_LONGSWORD.get())
            .add(ItemsRegistry.RUNIC_LONGSWORD.get());

        // ========== 双手武器 ==========
        tag(GeneHunterTags.TWO_HAND_WEAPON)
            // 巨剑
            .add(ItemsRegistry.IRON_CLAYMORE.get())
            .add(ItemsRegistry.GOLD_CLAYMORE.get())
            .add(ItemsRegistry.DIAMOND_CLAYMORE.get())
            .add(ItemsRegistry.NETHERITE_CLAYMORE.get())
            .add(ItemsRegistry.RUNIC_CLAYMORE.get())
            // 巨锤
            .add(ItemsRegistry.IRON_GREATHAMMER.get())
            .add(ItemsRegistry.GOLD_GREATHAMMER.get())
            .add(ItemsRegistry.DIAMOND_GREATHAMMER.get())
            .add(ItemsRegistry.NETHERITE_GREATHAMMER.get())
            .add(ItemsRegistry.RUNIC_GREATHAMMER.get())
            // 巨斧
            .add(ItemsRegistry.IRON_GREATAXE.get())
            .add(ItemsRegistry.GOLD_GREATAXE.get())
            .add(ItemsRegistry.DIAMOND_GREATAXE.get())
            .add(ItemsRegistry.NETHERITE_GREATAXE.get())
            .add(ItemsRegistry.RUNIC_GREATAXE.get());

        // ========== 长杆武器 ==========
        tag(GeneHunterTags.POLEARM_WEAPON)
            // 长矛
            .add(ItemsRegistry.IRON_SPEAR.get())
            .add(ItemsRegistry.GOLD_SPEAR.get())
            .add(ItemsRegistry.DIAMOND_SPEAR.get())
            .add(ItemsRegistry.NETHERITE_SPEAR.get())
            .add(ItemsRegistry.RUNIC_SPEAR.get())
            // 镰刀
            .add(ItemsRegistry.IRON_SCYTHE.get())
            .add(ItemsRegistry.GOLD_SCYTHE.get())
            .add(ItemsRegistry.DIAMOND_SCYTHE.get())
            .add(ItemsRegistry.NETHERITE_SCYTHE.get())
            .add(ItemsRegistry.RUNIC_SCYTHE.get())
            // 戟
            .add(ItemsRegistry.IRON_HALBERD.get())
            .add(ItemsRegistry.GOLD_HALBERD.get())
            .add(ItemsRegistry.DIAMOND_HALBERD.get())
            .add(ItemsRegistry.NETHERITE_HALBERD.get())
            .add(ItemsRegistry.RUNIC_HALBERD.get());
    }


}
