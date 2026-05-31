package org.galaxy.beyond.api.datagen.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondItemInit;

public class BeyondItemModelProvider extends ItemModelProvider {

    public BeyondItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Beyond.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(BeyondItemInit.LOOT_BAG.get());
        basicItem(BeyondItemInit.WORLD_SEED.get());
    }
}
