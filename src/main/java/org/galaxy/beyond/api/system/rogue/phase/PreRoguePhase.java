package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;

public class PreRoguePhase implements IRoguePhase {

    @Override
    public void enter(ServerLevel level, RogueContext ctx) {
        // 设置游戏种子（从进度种子派生）
        long seed = ctx.getGameSeed(level);
        if (seed == 0) {
            seed = level.getRandom().nextLong();
            ctx.setGameSeed(level, seed);
        }
        // 给所有玩家发放初始装备
        for (var p : ctx.inGamePlayers(level)) {
            ctx.getPlayerManager().handlePreRogue(p);
        }
    }
}
