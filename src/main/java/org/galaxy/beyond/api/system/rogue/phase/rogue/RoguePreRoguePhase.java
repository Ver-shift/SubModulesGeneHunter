package org.galaxy.beyond.api.system.rogue.phase.rogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

/**
 * PRE_ROGUE Phase：等待所有玩家使用战利品袋。
 * tick 检查全员 PlayerPhase == PreRogue，满足则推进到 INIT。
 */
public class RoguePreRoguePhase extends RoguePhase {

    private int tickCounter;

    public RoguePreRoguePhase() { super(BeyondPhaseInit.R_PRE_ROGUE); }

    @Override
    public void enter(ServerLevel level, IRogueContext ctx) {
        tickCounter = 0;
    }

    @Override
    public void tick(ServerLevel level, IRogueContext ctx) {
        tickCounter++;
        if (tickCounter % 20 != 0) return; // 每秒检查一次

        var players = ctx.playersInRogue(level);
        if (players.isEmpty()) return;

        int total = players.size();
        int ready = 0;
        for (var p : players) {
            if (ctx.getPlayerPhase(p) == BeyondPhaseInit.PLAYER_PRE_ROGUE.get()) ready++;
        }

        if (ready < total) {
            // 播报就绪状态
            for (var p : players) {
                p.sendSystemMessage(Component.translatable("beyond.rogue.ready_status", ready, total));
            }
            return;
        }

        // 校验关卡是否已设置
        var progressId = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig().getCurrentProgress();
        if (progressId == null) {
            level.getServer().getPlayerList()
                    .broadcastSystemMessage(Component.translatable("beyond.rogue.start.no_progress"), false);
            return;
        }

        // 全员就绪 → 推进到 INIT
        ctx.setPhase(level, BeyondPhaseInit.ROGUE_INIT.get());
    }

    @Override
    public void exit(ServerLevel level, IRogueContext ctx) {}
}
