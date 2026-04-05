package org.biotech.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.core.IXenoItem;
import org.biotech.api.system.trait.core.ITraitProvider;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Optional;

public class XeneItem extends Item implements IXenoItem<XeneItem>, ITraitProvider {

    public static final String SLOT_TYPE = BiotechAPI.XENE_EQUIP_SLOT;

    public XeneItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> map = LinkedHashMultimap.create();

        // 装备此物品时，动态添加1个 xene_equip_slot 槽位
        CuriosApi.addSlotModifier(map, SLOT_TYPE, id, 1, AttributeModifier.Operation.ADD_VALUE);

        return map;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        // Intentionally left empty: trait details are rendered by getTooltipImage() only.
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        return getTraitTooltipImage(stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return styleNameByTraits(stack, super.getName(stack));
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
        // XeneItem 不显示 Curios 生成的属性修饰 tooltip。
        return List.of();
    }


    /**
     * 无法手动取下
     * @param slotContext Context about the slot that the ItemStack is attempting to unequip from
     * @param stack       The ItemStack in question
     * @return true 允许卸下
     */
    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
