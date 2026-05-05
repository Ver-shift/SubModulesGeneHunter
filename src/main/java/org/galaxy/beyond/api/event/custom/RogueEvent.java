package org.galaxy.beyond.api.event.custom;

import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * 肉鸽系统，有多个注入点
 */
public abstract class RogueEvent extends LevelEvent {
    public RogueEvent(LevelAccessor level) {
        super(level);
    }

    public static class StartRogue extends RogueEvent{

        public StartRogue(LevelAccessor level) {
            super(level);
        }
    }
    //todo 拓展各个流程
}
