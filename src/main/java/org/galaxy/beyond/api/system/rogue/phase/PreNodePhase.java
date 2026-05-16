package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;

public class PreNodePhase implements IRoguePhase {

    @Override
    public void enter(ServerLevel level, RogueContext ctx) {
        // 通知所有玩家节点即将开始
    }

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        // 等待所有玩家到达节点位置，条件由Transitions.allPlayers处理
    }
}
