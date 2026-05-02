package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Beyond 模组物品注册。
 */
public class BeyondItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Beyond.MODID);

    /** 战利品袋：右键使用以启动一局游戏 */
    public static final DeferredItem<Item> LOOT_BAG =
            ITEMS.register("loot_bag", () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
