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
	 * 由具体物品实现：从自身结构中读取 TraitComp。
	 */
	TraitComp readTraitComp(ItemStack stack);

	default List<ITrait> getTraitsFromStack(ItemStack stack) {
		TraitComp comp = readTraitComp(stack);
		if (comp == null || comp.isEmpty()) {
			return List.of();
		}
		return comp.getTraits();
	}

	/**
	 * Legacy text tooltip path; keep for compatibility only.
	 * Prefer getTraitTooltipImage(ItemStack) for the new icon tooltip rendering.
	 */
	@Deprecated(since = "1.0.0")
	default void appendTraitHoverText(ItemStack stack, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		TooltipUtil.appendTraitGroupTooltip(readTraitComp(stack), tooltipComponents, tooltipFlag);
	}

	default Optional<TooltipComponent> getTraitTooltipImage(ItemStack stack) {
		return TooltipUtil.buildTraitTooltipComponent(readTraitComp(stack));
	}

	@Nullable
	default ResourceLocation getSelectedTraitTexture(ItemStack stack) {
		return TooltipUtil.getSelectedTraitTexture(readTraitComp(stack));
	}

	default ChatFormatting getColorByTraitCount(ItemStack stack) {
		int traitCount = getTraitsFromStack(stack).size();
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

	default Component styleNameByTraits(ItemStack stack, Component baseName) {
		ChatFormatting color = getColorByTraitCount(stack);
		if (color == null) {
			return baseName;
		}
		return baseName.copy().withStyle(color);
	}

}
