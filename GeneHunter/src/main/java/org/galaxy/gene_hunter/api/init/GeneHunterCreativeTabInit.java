package org.galaxy.gene_hunter.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.gene_hunter.GeneHunter;

public class GeneHunterCreativeTabInit {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GeneHunter.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GENE_HUNTER_TAB =
            TABS.register("gene_hunter_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.gene_hunter.gene_hunter_tab"))
                    .icon(() -> new ItemStack(GeneHunterItemInit.TEST_SINGLE_HAND_SWORD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(GeneHunterItemInit.TEST_SINGLE_HAND_SWORD.get());
                        output.accept(GeneHunterItemInit.TEST_ONE_HAND_DAMAGE_SWORD.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
