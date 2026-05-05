package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import org.galaxy.beyond.api.system.rogue.RogueState;

/**
 * NeoForge 标准事件：肉鸽游戏启动时发布。
 * 外部模组可通过 {@code @SubscribeEvent} 监听此事件。
 */
public class RogueStartEvent extends Event {

    private final ServerLevel level;
    private final RogueState state;

    public RogueStartEvent(ServerLevel level, RogueState state) {
        this.level = level;
        this.state = state;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public RogueState getState() {
        return state;
    }
}
