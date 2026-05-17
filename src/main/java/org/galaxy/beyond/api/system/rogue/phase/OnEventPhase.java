package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;
import org.galaxy.beyond.api.system.rogue.core.RogueState;

/**
 * ON_EVENT 阶段：触发事件、拓展区域、解锁节点、推进进度。
 */
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

        // 拓展活动区域
        var nodeData = ctx.nodeData(level);
        if (nodeData != null) {
            BeyondAPI.getBeyondManager().getZoneManager().addActiveZone(level, nodeData);
        }

        int currentIdx = ctx.getProgressManager().getCurrentProgressIndex(level);
        int total = ctx.getProgressManager().getTotalProgress(level);

        if (total > 0 && currentIdx >= total - 1) {
            ctx.setAllPlayerState(level, PlayerRogueState.REWARD);
            ctx.forceTo(level, RogueState.ROGUE_PROGRESS_FINISH);
        } else {
            ctx.getProgressManager().advanceProgress(level);
            ctx.setAllPlayerState(level, PlayerRogueState.ON_PROGRESS);
        }

        // 最后才标记节点解锁
        if (nodeData != null) {
            ctx.node().setNodeState(level, NodeState.UNLOCKED);
        }
    }
}
