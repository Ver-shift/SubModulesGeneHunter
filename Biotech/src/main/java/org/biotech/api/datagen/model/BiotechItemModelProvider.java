package org.biotech.api.datagen.model;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.biotech.api.init.BiotechItemInit;

public class BiotechItemModelProvider extends ItemModelProvider {
    public BiotechItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(BiotechItemInit.GENE_ITEM.get());
        basicItem(BiotechItemInit.XENE_ITEM.get());
        basicItem(BiotechItemInit.UNCOMMON_UNIDENTIFIED_GENE.get());
        basicItem(BiotechItemInit.RARE_UNIDENTIFIED_GENE.get());
        basicItem(BiotechItemInit.EPIC_UNIDENTIFIED_GENE.get());
    }
}
