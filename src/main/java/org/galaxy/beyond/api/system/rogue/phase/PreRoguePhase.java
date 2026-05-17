package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

public class PreRoguePhase implements IRoguePhase {

    private int tickCounter;

    @Override
    public void enter(ServerLevel level, RogueContext ctx) {
        long seed = ctx.getGameSeed(level);
        if (seed == 0) {
            seed = level.getRandom().nextLong();
            ctx.setGameSeed(level, seed);
        }
        tickCounter = 0;
    }

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        tickCounter++;
        if (tickCounter % 100 != 0) return; // 每5秒播报一次

        var players = ctx.inGamePlayers(level);
        int total = players.size();
        if (total == 0) return;

        int ready = 0;
        for (var p : players) {
            if (ctx.getPlayerManager().getState(p) == PlayerRogueState.PRE_ROGUE) ready++;
        }

        level.getServer().sendSystemMessage(
                Component.translatable("beyond.rogue.ready_status", ready, total));
    }
}
