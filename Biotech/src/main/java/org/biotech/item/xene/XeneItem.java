package org.biotech.item.xene;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.BiotechItemInit;
import org.biotech.api.system.gene.GeneDefinition;
import org.biotech.api.system.gene.core.IXenoItem;
import org.biotech.item.DefinedGeneItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

/**
 * An equipped xene expands the xene Curios slot type by one slot.
 * Therefore every newly equipped xene creates room for the next one.
 */
public class XeneItem extends DefinedGeneItem implements IXenoItem<XeneItem> {

    public static final String SLOT_TYPE = BiotechAPI.XENE_EQUIP_SLOT;

    public XeneItem() {
        super("xene");
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return SLOT_TYPE.equals(slotContext.identifier()) && hasDefinition(stack);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation modifierId, ItemStack stack) {
        // Datapack attributes are injected by XeneAttributeHandler.  Keep this
        // method dedicated to the per-equipped-stack slot expansion modifier.
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = com.google.common.collect.LinkedHashMultimap.create();
        CuriosApi.addSlotModifier(modifiers, SLOT_TYPE, modifierId, 1, AttributeModifier.Operation.ADD_VALUE);
        return modifiers;
    }

    public static ItemStack createForXene(ResourceLocation id, GeneDefinition definition) {
        return BiotechItemInit.XENE_ITEM.get().createDefinedStack(id, definition);
    }
}
