package org.biotech.api.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Common tooltip helpers unrelated to gameplay effects. */
public final class TooltipUtil {
    private TooltipUtil() {}

    /** Suppress Curios' redundant slot-type line for gene items. */
    public static List<Component> suppressCuriosSlotTypeTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
        return List.of();
    }
}
