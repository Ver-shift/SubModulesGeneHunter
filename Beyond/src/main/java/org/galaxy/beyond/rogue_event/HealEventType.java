package org.galaxy.beyond.rogue_event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

/**
 * 治疗事件 —— 恢复所有肉鸽玩家生命值。
 */
public class HealEventType extends RogueEventType {

    public static final ResourceLocation ID = Beyond.asResource("heal");

    public HealEventType() { super(ID); }

    @Override
    public void cast(Context context) {
        ServerLevel level = context.level();
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.event.heal.trigger"), false);

        level.getServer().getPlayerList().getPlayers().forEach(p -> {
            float newHealth = Math.min(p.getMaxHealth(), p.getHealth() + p.getMaxHealth() * 0.5f);
            p.setHealth(newHealth);
            p.sendSystemMessage(Component.translatable("beyond.event.heal.player"));
        });
    }

    @Override
    public Result next(Context context) {
        return Result.SUCCESS;
    }
}
