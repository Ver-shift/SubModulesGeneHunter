package org.biotech.api.datagen.model;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.biotech.api.init.ItemInit;

public class BiotechItemModelProvider extends ItemModelProvider {
    public BiotechItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ItemInit.GENE_ITEM.get());
        basicItem(ItemInit.XENE_ITEM.get());
        basicItem(ItemInit.UNCOMMON_UNIDENTIFIED_GENE.get());
        basicItem(ItemInit.RARE_UNIDENTIFIED_GENE.get());
        basicItem(ItemInit.EPIC_UNIDENTIFIED_GENE.get());
    }
}
