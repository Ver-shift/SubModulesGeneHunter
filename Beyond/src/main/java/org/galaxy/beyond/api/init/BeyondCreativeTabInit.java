package org.galaxy.beyond.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;

import java.util.function.Supplier;

public class BeyondCreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Beyond.MODID);

    public static final Supplier<CreativeModeTab> BEYOND_TAB = REGISTER.register("beyond_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.beyond"))
                    .icon(() -> new ItemStack(BeyondItemInit.LOOT_BAG.get()))
                    .displayItems((params, output) -> {
                        output.accept(BeyondBlockInit.NODE_BLOCK_ITEM.get());
                        output.accept(BeyondItemInit.LOOT_BAG.get());
                        output.accept(BeyondItemInit.TEST_ITEM.get());
                        output.accept(BeyondItemInit.TEST_IRON_SWORD.get());
                        output.accept(BeyondItemInit.WORLD_SEED.get());
                        output.accept(BeyondItemInit.HOME_TELEPORT_STONE.get());
                        output.accept(BeyondItemInit.ACTIVE_BOUNDARY_TELEPORT_STONE.get());
                        output.accept(BeyondItemInit.ANY_NODE_TELEPORT_STONE.get());
                        output.accept(BeyondItemInit.UNLOCKED_NODE_TELEPORT_STONE.get());
                        output.accept(BeyondItemInit.LOCKED_NODE_TELEPORT_STONE.get());
                        output.accept(BeyondItemInit.INSIDE_NODE_TELEPORT_STONE.get());
                        output.accept(BeyondItemInit.OUTSIDE_NODE_TELEPORT_STONE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
