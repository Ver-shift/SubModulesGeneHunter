package org.galaxy.gene_hunter.datagen.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.galaxy.gene_hunter.GeneHunter;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * GeneHunter 方块标签提供者
 */
public class GeneHunterBlockTagProvider extends BlockTagsProvider {

    public GeneHunterBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                      @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, GeneHunter.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 在这里添加方块标签
        // 示例：
        // tag(BlockTags.MINEABLE_WITH_PICKAXE)
        //     .add(GeneHunterBlockInit.YOUR_BLOCK.get());
    }
}
