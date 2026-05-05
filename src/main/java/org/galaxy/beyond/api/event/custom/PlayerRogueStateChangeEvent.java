package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueState;

/**
 * 玩家肉鸽状态变更事件。在 PlayerRogueState 发生改变时发布到 EVENT_BUS。
 * 外部模组通过 @SubscribeEvent 监听，实现逻辑扩展。
 */
public class PlayerRogueStateChangeEvent extends PlayerEvent {

    private final PlayerRogueState oldState;
    private final PlayerRogueState newState;

    public PlayerRogueStateChangeEvent(ServerPlayer player, PlayerRogueState oldState, PlayerRogueState newState) {
        super(player);
        this.oldState = oldState;
        this.newState = newState;
    }


    public PlayerRogueState getOldState() {
        return oldState;
    }

    public PlayerRogueState getNewState() {
        return newState;
    }
}
