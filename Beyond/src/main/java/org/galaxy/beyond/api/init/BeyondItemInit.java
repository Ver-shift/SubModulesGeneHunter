package org.galaxy.beyond.api.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.item.InfiniteIronSwordItem;
import org.galaxy.beyond.item.LootBag;
import org.galaxy.beyond.item.TextItem;
import org.galaxy.beyond.item.WorldSeedItem;

public class BeyondItemInit {

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Beyond.MODID);

    public static final DeferredItem<LootBag> LOOT_BAG =
            ITEMS.register("loot_bag", key -> new LootBag(new Item.Properties()));

    public static final DeferredItem<TextItem> TEST_ITEM =
            ITEMS.register("test_item", key -> new TextItem(new Item.Properties()));

    public static final DeferredItem<InfiniteIronSwordItem> TEST_IRON_SWORD =
            ITEMS.register("test_iron_sword", key -> new InfiniteIronSwordItem(new Item.Properties()));

    public static final DeferredItem<WorldSeedItem> WORLD_SEED =
            ITEMS.register("world_seed", key -> new WorldSeedItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
