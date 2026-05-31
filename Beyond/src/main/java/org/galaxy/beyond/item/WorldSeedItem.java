package org.galaxy.beyond.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.BeyondAPI;

public class WorldSeedItem extends Item {

    public WorldSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        boolean submitted = BeyondAPI.getBeyondManager().getZoneManager()
                .expandFromWorldSeed(serverLevel, serverPlayer.chunkPosition());
        if (submitted && !serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return new InteractionResultHolder<>(submitted ? InteractionResult.CONSUME : InteractionResult.FAIL, stack);
    }
}
