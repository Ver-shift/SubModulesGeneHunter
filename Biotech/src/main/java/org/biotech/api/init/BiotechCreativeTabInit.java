package org.biotech.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.item.GeneItem;
import org.biotech.item.xene.XeneItem;

public class BiotechCreativeTabInit {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GENE_TAB = TABS.register("spellbook_equipment", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Biotech.MODID + ".gene_tab"))
            .icon(() -> new ItemStack(BiotechItemInit.TEXT_ITEM.get()))
            .displayItems((parameters, entries) -> {
                // 未解析核心物品
                entries.accept(BiotechItemInit.TEXT_ITEM.get());
                entries.accept(BiotechItemInit.UNCOMMON_UNIDENTIFIED_GENE.get());
                entries.accept(BiotechItemInit.RARE_UNIDENTIFIED_GENE.get());
                entries.accept(BiotechItemInit.EPIC_UNIDENTIFIED_GENE.get());
                parameters.holders().lookupOrThrow(BiotechGeneInit.GENE_REGISTRY_KEY)
                        .listElements().forEach(holder ->
                                entries.accept(GeneItem.createForGene(holder.key().location(), holder.value())));
                parameters.holders().lookupOrThrow(BiotechGeneInit.XENE_REGISTRY_KEY)
                        .listElements().forEach(holder ->
                                entries.accept(XeneItem.createForXene(holder.key().location(), holder.value())));
            })
            .build());

}
