package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.*;

/**
 * PhaseRunner 替代了原来的 switch 驱动，此类仅保留数据层接口适配。
 */
public class RogueStateManager implements IRogueStateManager {

    private final PhaseRunner runner;
    private final RogueContext ctx;

    public RogueStateManager(PhaseRunner runner, RogueContext ctx) {
        this.runner = runner;
        this.ctx = ctx;
    }

    @Override
    public void tick(ServerLevel level) {
        // 主 tick 由 RogueManager 直接调用 runner.tick(level)
        // 此处保留作为兼容入口
        runner.tick(level);
    }

    @Override
    public void setRogueState(ServerLevel level, RogueState state) {
        runner.forceState(level, state);
    }

    @Override
    public RogueState getRogueState() {
        return runner.currentState();
    }

    @Override
    public PlayerRogueState isPlayerAllState(ServerLevel level) {
        return null;
    }

    @Override
    public boolean hasPlayerRogueState(ServerLevel level, PlayerRogueState state) {
        return ctx.anyPlayerMatch(level, state);
    }

    @Override
    public void setAllPlayerState(ServerLevel level, PlayerRogueState state) {
        ctx.setAllPlayerState(level, state);
    }

    @Override
    public void setCurrentNodeState(ServerLevel level, NodeState state) {
        // 节点状态同步由 Phase 内部处理
    }

    @Override
    public NodeState getCurrentNodeState() {
        return null;
    }
}
