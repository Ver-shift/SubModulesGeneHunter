package org.galaxy.beyond.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.item.LootBag;

public class BeyondItemInit {

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Beyond.MODID);

    public static final DeferredItem<LootBag> LOOT_BAG =
            ITEMS.register("loot_bag", key -> new LootBag(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, key))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
