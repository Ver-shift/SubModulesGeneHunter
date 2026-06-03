package org.galaxy.beyond.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.cap.ProgressStartCap;

/**
 * 战利品袋 —— 右键时进入肉鸽开袋流程。
 */
public class LootBag extends Item {
    public LootBag(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ProgressStartCap.tryOpenLootBag(serverPlayer, new RogueContext());
        }
        return InteractionResult.CONSUME;
    }
}
