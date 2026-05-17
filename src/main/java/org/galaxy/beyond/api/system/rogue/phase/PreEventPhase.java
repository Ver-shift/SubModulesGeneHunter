package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

/**
 * PRE_EVENT 阶段：初始化节点数据、抽取事件，设置所有玩家为 PRE_EVENT 状态。
 */
public class PreEventPhase implements IRoguePhase {

    @Override
    public void enter(ServerLevel level, RogueContext ctx) {
        ctx.node().handlePreEvent(level);
        ctx.setAllPlayerState(level, PlayerRogueState.PRE_EVENT);
    }

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        ctx.node().tick(level);
    }
}
