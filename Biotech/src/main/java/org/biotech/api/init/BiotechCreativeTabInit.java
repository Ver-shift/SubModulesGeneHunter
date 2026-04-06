package org.biotech.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.item.GeneItem;

@EventBusSubscriber
public class BiotechCreativeTabInit {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GENE_TAB = TABS.register("spellbook_equipment", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Biotech.MODID + ".gene_tab"))
            .icon(() -> new ItemStack(BiotechItemInit.TEXT_ITEM.get()))
            .displayItems((enabledFeatures, entries) -> {
                // 未解析核心物品
                entries.accept(BiotechItemInit.TEXT_ITEM.get());
                entries.accept(BiotechItemInit.UNCOMMON_UNIDENTIFIED_GENE.get());
                entries.accept(BiotechItemInit.RARE_UNIDENTIFIED_GENE.get());
                entries.accept(BiotechItemInit.EPIC_UNIDENTIFIED_GENE.get());
                entries.accept(BiotechItemInit.XENE_ITEM.get());
            })
            .build());

    @SubscribeEvent
    public static void fillGene(final BuildCreativeModeTabContentsEvent event){
        if (event.getTab() == GENE_TAB.get()) {
            // 添加所有基因对应的 GeneItem
            BiotechGeneInit.getAllGenes().forEach(gene -> {
                event.accept(GeneItem.createForGene(gene));
            });
        }
    }

}
