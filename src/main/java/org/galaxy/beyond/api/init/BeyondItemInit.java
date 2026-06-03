package org.galaxy.beyond.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.item.LootBag;
import org.galaxy.beyond.item.TextItem;
import org.galaxy.beyond.item.WorldSeedItem;

public class BeyondItemInit {

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Beyond.MODID);

    public static final DeferredItem<LootBag> LOOT_BAG =
            ITEMS.register("loot_bag", key -> new LootBag(itemProperties(key)));

    public static final DeferredItem<TextItem> TEST_ITEM =
            ITEMS.register("test_item", key -> new TextItem(itemProperties(key)));

    public static final DeferredItem<WorldSeedItem> WORLD_SEED =
            ITEMS.register("world_seed", key -> new WorldSeedItem(itemProperties(key)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static Item.Properties itemProperties(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }
}
