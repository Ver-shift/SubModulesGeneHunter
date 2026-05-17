package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;
import org.galaxy.beyond.api.system.rogue.core.RogueState;

public class OnEventPhase implements IRoguePhase {

    private boolean triggered;

    @Override
    public void enter(ServerLevel level, RogueContext ctx) {
        triggered = false;
        ctx.setAllPlayerState(level, PlayerRogueState.ON_EVENT);
    }

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        if (!triggered) {
            ctx.node().tick(level);
            triggered = true;
        }

        var nodeData = ctx.nodeData(level);
        if (nodeData != null) {
            BeyondAPI.getBeyondManager().getZoneManager().addActiveZone(level, nodeData);
        }

        int currentIdx = ctx.getProgressManager().getCurrentProgressIndex(level);
        int total = ctx.getProgressManager().getTotalProgress(level);

        if (total > 0 && currentIdx >= total - 1) {
            // 最后一个事件 → 解锁节点 + 结束
            if (nodeData != null) {
                ctx.node().setNodeState(level, NodeState.UNLOCKED);
            }
            ctx.setAllPlayerState(level, PlayerRogueState.REWARD);
            ctx.forceTo(level, RogueState.ROGUE_PROGRESS_FINISH);
        } else {
            // 不是最后一个 → 推进进度 + 回到 ON_PROGRESS 等下次触发
            ctx.getProgressManager().advanceProgress(level);
            ctx.node().setNodeState(level, NodeState.PRE_EVENT);
            ctx.setAllPlayerState(level, PlayerRogueState.ON_PROGRESS);
        }
    }
}
