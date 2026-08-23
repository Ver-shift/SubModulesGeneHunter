package org.biotech.api.system.gene.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.core.manager.IGeneInventoryManager;

/**
 * Shared use logic for unidentified gene cores.
 *
 * <p>The former trait-loot implementation was removed with the trait system.
 * Cores now resolve directly to a datapack-defined {@code GeneDefinition}.</p>
 */
public interface IUnidentifiedGeneItem {
    default InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand, Rarity rarity) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        IGeneInventoryManager manager = BiotechAPI.getGeneInventoryManager(player);
        if (manager == null) {
            player.sendSystemMessage(Component.translatable(
                    "loot.biotech.gene_trait.obtain.fail",
                    Component.translatable("loot.biotech.gene_trait.reason.unknown"), "Gene inventory is unavailable"));
            return InteractionResultHolder.fail(stack);
        }

        int targetSlot = manager.getFirstEmptySlot();
        IGeneInventoryManager.AddResult result = manager.addUnidentified(rarity);
        if (!result.isSuccess()) {
            player.sendSystemMessage(Component.translatable(
                    "loot.biotech.gene_trait.obtain.fail", getFailReason(result), result.getMessage())
                    .withStyle(getRarityColor(rarity)));
            return InteractionResultHolder.fail(stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        ItemStack resolvedGene = targetSlot >= 0 ? manager.getItemStack(targetSlot) : ItemStack.EMPTY;
        player.sendSystemMessage(Component.translatable(
                "loot.biotech.gene_trait.obtain.success",
                resolvedGene.isEmpty() ? Component.literal("Gene Core") : resolvedGene.getHoverName())
                .withStyle(getRarityColor(rarity)));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, getSoundPitchFromRarity(rarity));
        return InteractionResultHolder.consume(stack);
    }

    static float getSoundPitchFromRarity(Rarity rarity) {
        return switch (rarity) {
            case UNCOMMON -> 1.7F;
            case RARE -> 1.8F;
            case EPIC -> 1.9F;
            default -> 1.8F;
        };
    }

    static ChatFormatting getRarityColor(Rarity rarity) {
        return switch (rarity) {
            case UNCOMMON -> ChatFormatting.GREEN;
            case RARE -> ChatFormatting.BLUE;
            case EPIC -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.WHITE;
        };
    }

    private static Component getFailReason(IGeneInventoryManager.AddResult result) {
        return switch (result) {
            case FULL -> Component.translatable("loot.biotech.gene_trait.reason.full");
            case EMPTY_ITEM -> Component.translatable("loot.biotech.gene_trait.reason.empty_item");
            case NO_SPACE -> Component.translatable("loot.biotech.gene_trait.reason.no_space");
            case INVALID_TYPE -> Component.translatable("loot.biotech.gene_trait.reason.invalid_type");
            default -> Component.translatable("loot.biotech.gene_trait.reason.unknown");
        };
    }
}
