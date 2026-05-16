package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

public class RogueProgressFinishPhase implements IRoguePhase {

    @Override
    public void enter(ServerLevel level, RogueContext ctx) {
        // 所有玩家进入奖励结算阶段
        ctx.setAllPlayerState(level, PlayerRogueState.REWARD);
    }

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        // 检测所有玩家是否已完成结算
        boolean allDone = true;
        for (var p : ctx.inGamePlayers(level)) {
            var state = ctx.getPlayerManager().getState(p);
            if (state != PlayerRogueState.LOBBY) {
                allDone = false;
                // 触发单个玩家的结算 + 传送回安全区
                if (state == PlayerRogueState.PROGRESS_FINISH) {
                    ctx.getPlayerManager().playerIntoSafeZone(p);
                }
            }
        }
        // 所有玩家回到安全区后，重置全局状态
        if (allDone) {
            ctx.setState(level, org.galaxy.beyond.api.system.rogue.core.RogueState.LOBBY);
        }
    }
}
