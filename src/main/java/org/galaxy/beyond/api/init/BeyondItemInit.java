package org.galaxy.beyond.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.item.LootBag;

import java.util.function.Supplier;

public class BeyondItemInit {
    public static final DeferredRegister<Item> REGISTER =
            DeferredRegister.create(Registries.ITEM, Beyond.MODID);

    public static final Supplier<Item> LOOT_BAG =
            REGISTER.register("loot_bag", LootBag::new);

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
