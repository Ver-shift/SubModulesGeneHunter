package org.biotech.api.system.trait.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.biotech.api.util.TooltipUtil;
import org.biotech.component.TraitComp;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 给geneItem 或者xeneItem 添加ITrait
 */
public interface ITraitProvider {

	/**
	 * Legacy hook kept for compatibility.
	 * Prefer ItemStack-level access via {@link IStackTraitAccess#getTraitComp(ItemStack)}.
	 */
	@Deprecated(since = "1.0.0")
	default TraitComp readTraitComp(ItemStack stack) {
		return IStackTraitAccess.getTraitComp(stack);
	}

	default List<ITrait> getTraitsFromStack(ItemStack stack) {
		return IStackTraitAccess.getTraits(stack);
	}

	/**
	 * Legacy text tooltip path; keep for compatibility only.
	 * Prefer getTraitTooltipImage(ItemStack) for the new icon tooltip rendering.
	 */
	@Deprecated(since = "1.0.0")
	default void appendTraitHoverText(ItemStack stack, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		TooltipUtil.appendTraitGroupTooltip(IStackTraitAccess.getTraitComp(stack), tooltipComponents, tooltipFlag);
	}

	default Optional<TooltipComponent> getTraitTooltipImage(ItemStack stack) {
		return IStackTraitAccess.getTraitTooltipImage(stack);
	}

	@Nullable
	default ResourceLocation getSelectedTraitTexture(ItemStack stack) {
		return IStackTraitAccess.getSelectedTraitTexture(stack);
	}

	default ChatFormatting getColorByTraitCount(ItemStack stack) {
		return IStackTraitAccess.getColorByTraitCount(stack);
	}

	default Component styleNameByTraits(ItemStack stack, Component baseName) {
		return IStackTraitAccess.styleNameByTraits(stack, baseName);
	}

}
