package org.biotech.api.system.trait.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.biotech.api.util.TooltipUtil;
import org.biotech.component.TraitComp;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Exposes trait-related helpers directly on {@link ItemStack} via mixin.
 */
public interface IStackTraitAccess {

    @Nullable
    TraitComp biotech$getTraitComp();

    default List<ITrait> biotech$getTraits() {
        TraitComp comp = biotech$getTraitComp();
        if (comp == null || comp.isEmpty()) {
            return List.of();
        }
        return comp.getTraits();
    }

    default int biotech$getTraitCount() {
        return biotech$getTraits().size();
    }

    default Optional<TooltipComponent> biotech$getTraitTooltipImage() {
        return TooltipUtil.buildTraitTooltipComponent(biotech$getTraitComp());
    }

    @Nullable
    default ResourceLocation biotech$getSelectedTraitTexture() {
        return TooltipUtil.getSelectedTraitTexture(biotech$getTraitComp());
    }

    @Nullable
    default ChatFormatting biotech$getColorByTraitCount() {
        int traitCount = biotech$getTraitCount();
        if (traitCount == 1) {
            return ChatFormatting.WHITE;
        }
        if (traitCount == 2) {
            return ChatFormatting.BLUE;
        }
        if (traitCount >= 3) {
            return ChatFormatting.LIGHT_PURPLE;
        }
        return null;
    }

    default Component biotech$styleNameByTraits(Component baseName) {
        ChatFormatting color = biotech$getColorByTraitCount();
        if (color == null) {
            return baseName;
        }
        return baseName.copy().withStyle(color);
    }

    static IStackTraitAccess of(ItemStack stack) {
        return (IStackTraitAccess) (Object) stack;
    }

    @Nullable
    static TraitComp getTraitComp(ItemStack stack) {
        return of(stack).biotech$getTraitComp();
    }

    static List<ITrait> getTraits(ItemStack stack) {
        return of(stack).biotech$getTraits();
    }

    static int getTraitCount(ItemStack stack) {
        return of(stack).biotech$getTraitCount();
    }

    static Optional<TooltipComponent> getTraitTooltipImage(ItemStack stack) {
        return of(stack).biotech$getTraitTooltipImage();
    }

    @Nullable
    static ResourceLocation getSelectedTraitTexture(ItemStack stack) {
        return of(stack).biotech$getSelectedTraitTexture();
    }

    @Nullable
    static ChatFormatting getColorByTraitCount(ItemStack stack) {
        return of(stack).biotech$getColorByTraitCount();
    }

    static Component styleNameByTraits(ItemStack stack, Component baseName) {
        return of(stack).biotech$styleNameByTraits(baseName);
    }
}

