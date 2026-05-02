package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Beyond 模组创造模式物品栏。
 */
public class BeyondCreativeTab {

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Beyond.MODID);

    public static final Supplier<CreativeModeTab> BEYOND_TAB = TABS.register("beyond_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.beyond.beyond_tab"))
                    .icon(() -> new ItemStack(BeyondItems.LOOT_BAG.get()))
                    .displayItems((enabledFeatures, entries) -> {
                        entries.accept(BeyondItems.LOOT_BAG.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
