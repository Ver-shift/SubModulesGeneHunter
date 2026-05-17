package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;
import org.galaxy.beyond.api.system.rogue.core.RogueState;

public class OnProgressPhase implements IRoguePhase {

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        // 进度完成 → 全局结算
        int current = ctx.getProgressManager().getCurrentProgressIndex(level);
        int total = ctx.getProgressManager().getTotalProgress(level);
        if (total > 0 && current >= total) {
            ctx.forceTo(level, RogueState.ROGUE_PROGRESS_FINISH);
            return;
        }

        // 单人自动推进：PRE_EVENT 状态且全员就绪 → 自动进入 ON_EVENT
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        if (cfg.getRoguePlayerIds().size() == 1) {
            NodeState nodeState = ctx.node().getNodeState(level);
            if (nodeState == NodeState.PRE_EVENT && ctx.allPlayersMatch(level, PlayerRogueState.ON_PROGRESS)) {
                ctx.node().setNodeState(level, NodeState.ON_EVENT);
                ctx.setAllPlayerState(level, PlayerRogueState.PRE_NODE);
            }
        }
    }
}
