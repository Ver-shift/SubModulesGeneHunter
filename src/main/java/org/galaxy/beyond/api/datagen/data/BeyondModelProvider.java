package org.galaxy.beyond.api.datagen.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondItemInit;

public class BeyondModelProvider extends ModelProvider {

    public BeyondModelProvider(PackOutput output) {
        super(output, Beyond.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(BeyondItemInit.LOOT_BAG.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(BeyondItemInit.TEST_ITEM.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(BeyondItemInit.WORLD_SEED.get(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    public String getName() {
        return "Beyond Models";
    }
}
