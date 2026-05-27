package org.galaxy.beyond.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;

/**
 * 战利品袋 —— 使用事件由 {@code BeyondManagerEventHandle.onUse} 转发到 Zone Cap。
 */
public class LootBag extends Item {
    public LootBag(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer) {
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}
