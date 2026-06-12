package org.galaxy.beyond.api.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.item.InfiniteIronSwordItem;
import org.galaxy.beyond.item.LootBag;
import org.galaxy.beyond.item.TeleportStoneItem;
import org.galaxy.beyond.item.TextItem;
import org.galaxy.beyond.item.WorldSeedItem;
import org.galaxy.beyond.api.system.teleport.BeyondTeleportType;

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

    public static final DeferredItem<TeleportStoneItem> HOME_TELEPORT_STONE =
            ITEMS.register("home_teleport_stone", key -> new TeleportStoneItem(new Item.Properties(), BeyondTeleportType.HOME));

    public static final DeferredItem<TeleportStoneItem> ACTIVE_BOUNDARY_TELEPORT_STONE =
            ITEMS.register("active_boundary_teleport_stone", key -> new TeleportStoneItem(new Item.Properties(), BeyondTeleportType.ACTIVE_BOUNDARY));

    public static final DeferredItem<TeleportStoneItem> ANY_NODE_TELEPORT_STONE =
            ITEMS.register("any_node_teleport_stone", key -> new TeleportStoneItem(new Item.Properties(), BeyondTeleportType.ANY_NODE));

    public static final DeferredItem<TeleportStoneItem> UNLOCKED_NODE_TELEPORT_STONE =
            ITEMS.register("unlocked_node_teleport_stone", key -> new TeleportStoneItem(new Item.Properties(), BeyondTeleportType.UNLOCKED_NODE));

    public static final DeferredItem<TeleportStoneItem> LOCKED_NODE_TELEPORT_STONE =
            ITEMS.register("locked_node_teleport_stone", key -> new TeleportStoneItem(new Item.Properties(), BeyondTeleportType.LOCKED_NODE));

    public static final DeferredItem<TeleportStoneItem> INSIDE_NODE_TELEPORT_STONE =
            ITEMS.register("inside_node_teleport_stone", key -> new TeleportStoneItem(new Item.Properties(), BeyondTeleportType.INSIDE_NODE));

    public static final DeferredItem<TeleportStoneItem> OUTSIDE_NODE_TELEPORT_STONE =
            ITEMS.register("outside_node_teleport_stone", key -> new TeleportStoneItem(new Item.Properties(), BeyondTeleportType.OUTSIDE_NODE));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
