package org.galaxy.beyond.rogue_event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.rogue.player.RoguePlayerManager;

/**
 * 奖励事件 —— 向所有肉鸽玩家发放铁锭，战利品袋同款发放逻辑。
 */
public class RewardEventType extends RogueEventType {

    public static final ResourceLocation ID = Beyond.asResource("reward");

    public RewardEventType() { super(ID); }

    @Override
    public void cast(Context context) {
        ServerLevel level = context.level();
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.event.reward.grant"), false);

        var ctx = new RogueContext();
        for (var player : ctx.playersInRogue(level)) {
            RoguePlayerManager.giveItem(player, Items.IRON_INGOT);
        }
    }

    @Override
    public Result next(Context context) {
        return Result.SUCCESS;
    }
}
