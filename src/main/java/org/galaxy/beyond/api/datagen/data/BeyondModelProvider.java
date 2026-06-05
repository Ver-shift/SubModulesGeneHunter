package org.galaxy.beyond.api.datagen.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondBlockInit;
import org.galaxy.beyond.api.init.BeyondItemInit;

public class BeyondModelProvider extends ModelProvider {

    public BeyondModelProvider(PackOutput output) {
        super(output, Beyond.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockState(blockModels, BeyondBlockInit.NODE_BLOCK.get());

        flatItem(itemModels, BeyondItemInit.LOOT_BAG.get());
        flatItem(itemModels, BeyondItemInit.TEST_ITEM.get());
        flatItem(itemModels, BeyondItemInit.WORLD_SEED.get());
        blockItem(itemModels, BeyondBlockInit.NODE_BLOCK_ITEM.get(), BeyondBlockInit.NODE_BLOCK.get());
    }

    @Override
    public String getName() {
        return "beyond_item_models";
    }

    private void flatItem(ItemModelGenerators itemModels, Item item) {
        itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    private void blockState(BlockModelGenerators blockModels, Block block) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block,
                BlockModelGenerators.variant(BlockModelGenerators.plainModel(ModelLocationUtils.getModelLocation(block)))));
    }

    private void blockItem(ItemModelGenerators itemModels, Item item, Block block) {
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(block)));
    }
}
