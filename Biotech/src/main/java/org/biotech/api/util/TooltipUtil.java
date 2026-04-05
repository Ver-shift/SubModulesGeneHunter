package org.biotech.api.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.biotech.api.tooltip.TraitTooltipComponent;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.component.TraitComp;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TooltipUtil {

	private TooltipUtil() {
	}

	/**
	 * 渲染完整天赋组信息（标题、数量、逐条详情、可选高级ID）。
	 * @deprecated Legacy text tooltip builder; prefer buildTraitTooltipComponent(TraitComp).
	 */
	@Deprecated(since = "1.0.0")
	public static void appendTraitGroupTooltip(TraitComp traitComp, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		tooltipComponents.add(Component.translatable("item.biotech.xene_item.tooltip.title")
				.withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

		if (traitComp == null || traitComp.isEmpty()) {
			tooltipComponents.add(Component.translatable("item.biotech.xene_item.tooltip.empty")
					.withStyle(ChatFormatting.GRAY));
			return;
		}

		tooltipComponents.add(Component.translatable("item.biotech.xene_item.tooltip.count", traitComp.size())
				.withStyle(ChatFormatting.DARK_AQUA));

		int traitIndex = 1;
		for (ITrait trait : traitComp.getTraits()) {
			if (trait == null) {
				continue;
			}

			tooltipComponents.add(Component.translatable("item.biotech.xene_item.tooltip.trait_index", traitIndex++)
					.withStyle(ChatFormatting.GOLD));

			List<? extends Component> uniqueInfo = trait.getUniqueInfo();
			if (uniqueInfo == null || uniqueInfo.isEmpty()) {
				tooltipComponents.add(Component.translatable("item.biotech.xene_item.tooltip.unknown")
						.withStyle(ChatFormatting.GRAY));
			} else {
				for (Component info : uniqueInfo) {
					tooltipComponents.add(Component.literal("  - ")
							.withStyle(ChatFormatting.DARK_GRAY)
							.append(info.copy().withStyle(ChatFormatting.GREEN)));
				}
			}

			if (tooltipFlag.isAdvanced()) {
				tooltipComponents.add(Component.translatable("item.biotech.xene_item.tooltip.trait_id", trait.getId())
						.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			}
		}
	}

	/**
	 * 屏蔽 Curios 默认的 slotType tooltip。
	 */
	public static List<Component> suppressCuriosSlotTypeTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
		return List.of();
	}

	public static Optional<TooltipComponent> buildTraitTooltipComponent(TraitComp traitComp) {
		if (traitComp == null || traitComp.isEmpty()) {
			return Optional.empty();
		}

		TraitTooltipComponent component = TraitTooltipComponent.fromTraitComp(traitComp);
		if (component.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(component);
	}

	@Nullable
	public static ResourceLocation getSelectedTraitTexture(TraitComp traitComp) {
		if (traitComp == null || traitComp.isEmpty()) {
			return null;
		}

		for (ITrait trait : traitComp.getTraits()) {
			if (trait == null) {
				continue;
			}
			ResourceLocation texture = trait.getTexture();
			if (texture != null) {
				return texture;
			}
		}
		return null;
	}
}
