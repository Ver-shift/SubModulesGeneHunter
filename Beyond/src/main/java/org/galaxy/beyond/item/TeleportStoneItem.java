package org.galaxy.beyond.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.teleport.BeyondTeleportActions;
import org.galaxy.beyond.api.system.teleport.BeyondTeleportType;

public class TeleportStoneItem extends Item {

    private final BeyondTeleportType type;

    public TeleportStoneItem(Properties properties, BeyondTeleportType type) {
        super(properties.stacksTo(16));
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!BeyondTeleportActions.teleport(serverPlayer, type)) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
    }
}
