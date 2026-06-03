package org.galaxy.beyond.api.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.block.NodeBlock;

public class BeyondBlockInit {

    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Beyond.MODID);

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Beyond.MODID);

    public static final DeferredBlock<NodeBlock> NODE_BLOCK =
            BLOCKS.registerBlock("node_block", NodeBlock::new,
                    () -> BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.METAL).noOcclusion());

    public static final DeferredItem<BlockItem> NODE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(NODE_BLOCK);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
