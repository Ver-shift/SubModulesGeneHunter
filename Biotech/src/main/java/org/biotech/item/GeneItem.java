package org.biotech.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.BiotechItemInit;
import org.biotech.api.system.gene.GeneDefinition;
import org.biotech.api.system.gene.core.IGeneItem;
import top.theillusivec4.curios.api.SlotContext;

/** The sole Curios item type used to carry a datapack-defined gene. */
public class GeneItem extends DefinedGeneItem implements IGeneItem<GeneItem> {
    public GeneItem() {
        super("gene");
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return BiotechAPI.GENE_EQUIP_SLOT.equals(slotContext.identifier()) && hasDefinition(stack);
    }

    public static ItemStack createForGene(ResourceLocation id, GeneDefinition definition) {
        return BiotechItemInit.GENE_ITEM.get().createDefinedStack(id, definition);
    }
}
