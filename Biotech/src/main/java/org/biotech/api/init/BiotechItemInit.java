package org.biotech.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.item.*;
import org.biotech.item.xene.XeneItem;

public class BiotechItemInit {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredHolder<Item, Item> TEXT_ITEM;
    public static final DeferredHolder<Item, GeneItem> GENE_ITEM;
    
    // 未解析核心 - 按稀有度分级
    public static final DeferredHolder<Item, UncommonUnidentifiedGeneItem> UNCOMMON_UNIDENTIFIED_GENE;
    public static final DeferredHolder<Item, RareUnidentifiedGeneItem> RARE_UNIDENTIFIED_GENE;
    public static final DeferredHolder<Item, EpicUnidentifiedGeneItem> EPIC_UNIDENTIFIED_GENE;
    public static final DeferredHolder<Item, XeneItem> XENE_ITEM;

    static {
        TEXT_ITEM = ITEMS.register("text_item", TextGeneItem::new);
        GENE_ITEM = ITEMS.register("gene_item", GeneItem::new);
        XENE_ITEM = ITEMS.register("xene_item", XeneItem::new);

        // 注册各稀有度未解析核心
        UNCOMMON_UNIDENTIFIED_GENE = ITEMS.register("uncommon_unidentified_gene", UncommonUnidentifiedGeneItem::new);
        RARE_UNIDENTIFIED_GENE = ITEMS.register("rare_unidentified_gene", RareUnidentifiedGeneItem::new);
        EPIC_UNIDENTIFIED_GENE = ITEMS.register("epic_unidentified_gene", EpicUnidentifiedGeneItem::new);
    }
}
