package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;

/**
 * Phase 驱动器 —— Phase 为纯标记枚举，逻辑通过 {@link org.galaxy.beyond.api.system.rogue.RogueCapManager} 分发到 RogueCap。
 */
public class PhaseRunner {

    private final IRogueContext ctx;
    private RoguePhase lastGlobal;
    private NodePhase lastNode;

    public PhaseRunner(IRogueContext ctx) {
        this.ctx = ctx;
    }

    public void tick(ServerLevel level) {
        tickGlobal(level);
        tickNode(level);
    }

    private void tickGlobal(ServerLevel level) {
        RoguePhase cur = ctx.getPhase(level);
        if (cur != lastGlobal) {
            if (lastGlobal != null) {
                dispatchPhaseExit(level, lastGlobal, cur);
            }
            lastGlobal = cur;
            if (cur != null) {
                dispatchPhaseEnter(level, lastGlobal != null ? lastGlobal : cur, cur);
            }
        }
        if (cur != null) {
            dispatchPhaseTick(level, cur);
        }
    }

    private void tickNode(ServerLevel level) {
        var nodeData = ctx.getRogueData(level).getRogueNodeData();
        if (nodeData == null || nodeData.getNodeData() == null) return;
        NodePhase cur = nodeData.getNodeData().getPhase();
        if (cur != lastNode) {
            if (lastNode != null) {
                dispatchPhaseExit(level, lastNode, cur);
            }
            lastNode = cur;
            if (cur != null) {
                dispatchPhaseEnter(level, lastNode != null ? lastNode : cur, cur);
            }
        }
        if (cur != null) {
            dispatchPhaseTick(level, cur);
        }
    }

    public void forceState(ServerLevel level, RoguePhase target) {
        RoguePhase cur = ctx.getPhase(level);
        if (target == cur && lastGlobal != null) return;
        if (cur != null) {
            dispatchPhaseExit(level, cur, target);
        }
        ctx.setPhase(level, target);
        lastGlobal = target;
        if (target != null) {
            dispatchPhaseEnter(level, cur, target);
        }
    }

    private static void dispatchPhaseEnter(ServerLevel level, Phase from, Phase to) {
        BeyondAPI.getBeyondManager().getRogueCapManager().dispatchPhaseEnter(level, from, to);
    }

    private static void dispatchPhaseExit(ServerLevel level, Phase from, Phase to) {
        BeyondAPI.getBeyondManager().getRogueCapManager().dispatchPhaseExit(level, from, to);
    }

    private static void dispatchPhaseTick(ServerLevel level, Phase phase) {
        BeyondAPI.getBeyondManager().getRogueCapManager().dispatchPhaseTick(level, phase);
    }
}
