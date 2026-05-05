package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.rogue.RogueState;

/**
 * 全局肉鸽状态变更事件。在 RogueState 发生改变时发布到 EVENT_BUS。
 */
public class RogueStateChangeEvent extends LevelEvent {

    private final RogueState oldState;
    private final RogueState newState;

    public RogueStateChangeEvent(ServerLevel level, RogueState oldState, RogueState newState) {
        super(level);
        this.oldState = oldState;
        this.newState = newState;
    }


    public RogueState getOldState() {
        return oldState;
    }
    public RogueState getNewState() {
        return newState;
    }
}
