package org.galaxy.beyond.api.system.rogue.phase.rogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.rogue.player.RoguePlayerManager;

/**
 * 结算 Phase：清空所有玩家背包 ValueComp 物品，播报总价值，返回安全区。
 */
public class RogueProgressFinishPhase extends RoguePhase {

    public RogueProgressFinishPhase() { super(BeyondPhaseInit.R_PROGRESS_FINISH); }

    @Override
    public void enter(ServerLevel level, IRogueContext ctx) {
        var players = ctx.playersInRogue(level);
        for (var p : players) {
            int val = RoguePlayerManager.clearInventory(p);
            p.sendSystemMessage(Component.translatable("beyond.rogue.reward", val));
            ctx.setPlayerPhase(p, BeyondPhaseInit.PLAYER_PROGRESS_FINISH.get());
        }
    }

    @Override
    public void tick(ServerLevel level, IRogueContext ctx) {}

    @Override
    public void exit(ServerLevel level, IRogueContext ctx) {}
}
