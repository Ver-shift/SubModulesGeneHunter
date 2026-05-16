package org.galaxy.beyond.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.BeyondAPI;

/**
 * 用于启动游戏
 */
public class LootBag extends Item {
    public LootBag(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer
                && BeyondAPI.getBeyondManager().getRogueManager().getPlayerRougeManager().isInRogue(serverPlayer)

        ) {
            BeyondAPI.getBeyondManager().getRogueManager().getPlayerRougeManager().useLootBag(serverPlayer);
            if (!player.getAbilities().instabuild) {
                player.getItemInHand(hand).shrink(1);
            }
        }else {
            player.sendSystemMessage(Component.translatable("beyond.rogue.not_in_game"));
        }


        return super.use(level, player, hand);
    }
}
