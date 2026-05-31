package org.biotech.api.system.gene.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.biotech.api.config.ServerConfig;
import org.biotech.api.init.BiotechLootTypeInit;
import org.biotech.api.system.gene.core.manager.IGeneInventoryManager;
import org.biotech.api.init.BiotechAttributeInit;
import org.biotech.loot.GeneTraitLootType;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.random.RandomManager;

public interface IUnidentifiedGeneItem {

    /**
     * 抽出一个词条基因，并且消耗自身
     * @param player
     */
    default void use(Player player, Rarity rarity){
        if (player instanceof ServerPlayer serverPlayer){
            AttributeInstance instance = serverPlayer.getAttribute(BiotechAttributeInit.GENE_TRAIT_ROLL_COUNT);
            if (instance != null) {
                instance.setBaseValue(getCountFromRarity(rarity));
            }

            var lootType = BiotechLootTypeInit.GENE_TRAIT_LOOT_TYPE.get();
            LootManager.Context context = LootManager.Context.of(serverPlayer, RandomManager.PROGRESS_RANDOM_ID);
            LootManager.Request request = LootManager.Request.builder()
                    .lootType(lootType)
                    .rolls(ILootType.getPoolCountFromAttribute(getCountFromRarity(rarity), context.random()))
                    .build();
            var result = GalaxyLibAPI.getLootManager().roll(request, context);
            if (!(lootType instanceof GeneTraitLootType geneTraitLootType)) return;

            GeneTraitLootType.ClaimResult claimResult = geneTraitLootType.claimResultsToPlayerAndReturn(serverPlayer, result);
            sendGeneTraitObtainMessage(serverPlayer, rarity, claimResult);
        }
    }

    /**
     * 统一发送基因核心获取结果提示。
     */
    static void sendGeneTraitObtainMessage(ServerPlayer player, Rarity rarity, GeneTraitLootType.ClaimResult claimResult) {
        ChatFormatting rarityColor = getRarityColor(rarity);
        IGeneInventoryManager.AddResult addResult = claimResult.addResult();
        if (addResult.isSuccess()) {
            player.sendSystemMessage(Component.translatable(
                    "loot.biotech.gene_trait.obtain.success",
                    claimResult.stack().isEmpty() ? Component.literal("Gene Core") : claimResult.stack().getHoverName(),
                    claimResult.traitCount()
            ).withStyle(rarityColor));
            return;
        }

        player.sendSystemMessage(Component.translatable(
                "loot.biotech.gene_trait.obtain.fail",
                getFailReason(addResult),
                addResult.getMessage()
        ).withStyle(rarityColor));
    }

    /**
     * 三档分级统一使用流程：UNCOMMON / RARE / EPIC。
     */
    default InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand, Rarity rarity) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (!level.isClientSide) {
            use(player, rarity);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.6F,
                    getSoundPitchFromRarity(rarity)
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }



    static float getCountFromRarity(Rarity rarity){
        return switch (rarity) {
            case UNCOMMON -> ServerConfig.getUncommonGeneTraitRollCount();
            case RARE -> ServerConfig.getRareGeneTraitRollCount();
            case EPIC -> ServerConfig.getEpicGeneTraitRollCount();

            default -> 0;
        };


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
