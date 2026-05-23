package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.event.custom.PhaseChangeEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Phase 驱动器 —— Phase 为纯标记枚举，逻辑通过 {@link org.galaxy.beyond.api.system.rogue.RogueCapManager} 分发到 RogueCap。
 * <p>
 * 每次 phase 变更 / tick 均发布 {@link PhaseChangeEvent} 到 NeoForge EVENT_BUS。
 */
public class PhaseRunner {

    private final IRogueContext ctx;
    private RoguePhase lastGlobal;
    private NodePhase lastNode;
    private final Map<UUID, PlayerPhase> lastPlayerPhase = new HashMap<>();

    public PhaseRunner(IRogueContext ctx) {
        this.ctx = ctx;
    }

    public void tick(ServerLevel level) {
        tickGlobal(level);
        tickNode(level);
        tickPlayers(level);
    }

    // ---- 全局 RoguePhase ----

    private void tickGlobal(ServerLevel level) {
        RoguePhase cur = ctx.getPhase(level);
        if (cur != lastGlobal) {
            if (lastGlobal != null) {
                post(new PhaseChangeEvent.RogueExit(level, lastGlobal, cur));
                dispatchPhaseExit(level, lastGlobal, cur);
            }
            Phase from = lastGlobal != null ? lastGlobal : cur;
            lastGlobal = cur;
            if (cur != null) {
                post(new PhaseChangeEvent.RogueEnter(level, from, cur));
                dispatchPhaseEnter(level, from, cur);
            }
        }
        if (cur != null) {
            post(new PhaseChangeEvent.RogueTick(level, cur));
            dispatchPhaseTick(level, cur);
        }
    }

    // ---- 节点 NodePhase ----

    private void tickNode(ServerLevel level) {
        var nodeData = ctx.getRogueData(level).getRogueNodeData();
        if (nodeData == null || nodeData.getNodeData() == null) return;
        NodePhase cur = nodeData.getNodeData().getPhase();
        if (cur != lastNode) {
            if (lastNode != null) {
                post(new PhaseChangeEvent.NodeExit(level, lastNode, cur));
                dispatchPhaseExit(level, lastNode, cur);
            }
            Phase from = lastNode != null ? lastNode : cur;
            lastNode = cur;
            if (cur != null) {
                post(new PhaseChangeEvent.NodeEnter(level, from, cur));
                dispatchPhaseEnter(level, from, cur);
            }
        }
        if (cur != null) {
            post(new PhaseChangeEvent.NodeTick(level, cur));
            dispatchPhaseTick(level, cur);
        }
    }

    // ---- 玩家 PlayerPhase ----

    private void tickPlayers(ServerLevel level) {
        for (ServerPlayer p : ctx.playersInRogue(level)) {
            PlayerPhase cur = ctx.getPlayerPhase(p);
            PlayerPhase last = lastPlayerPhase.get(p.getUUID());
            if (cur != last) {
                if (last != null) {
                    post(new PhaseChangeEvent.PlayerExit(last, cur, p));
                }
                Phase from = last != null ? last : cur;
                lastPlayerPhase.put(p.getUUID(), cur);
                if (cur != null) {
                    post(new PhaseChangeEvent.PlayerEnter(from, cur, p));
                }
            }
            if (cur != null) {
                post(new PhaseChangeEvent.PlayerTick(cur, p));
            }
        }
    }

    // ---- 强制状态跳转 ----

    public void forceState(ServerLevel level, RoguePhase target) {
        RoguePhase cur = ctx.getPhase(level);
        if (target == cur && lastGlobal != null) return;
        if (cur != null) {
            post(new PhaseChangeEvent.RogueExit(level, cur, target));
            dispatchPhaseExit(level, cur, target);
        }
        ctx.setPhase(level, target);
        lastGlobal = target;
        if (target != null) {
            post(new PhaseChangeEvent.RogueEnter(level, cur, target));
            dispatchPhaseEnter(level, cur, target);
        }
    }

    // ---- 内部分发 ----

    private static void dispatchPhaseEnter(ServerLevel level, Phase from, Phase to) {
        BeyondAPI.getBeyondManager().getRogueCapManager().dispatchPhaseEnter(level, from, to);
    }

    private static void dispatchPhaseExit(ServerLevel level, Phase from, Phase to) {
        BeyondAPI.getBeyondManager().getRogueCapManager().dispatchPhaseExit(level, from, to);
    }

    private static void dispatchPhaseTick(ServerLevel level, Phase phase) {
        BeyondAPI.getBeyondManager().getRogueCapManager().dispatchPhaseTick(level, phase);
    }

    private static <T extends PhaseChangeEvent> void post(T event) {
        PhaseChangeEvent.post(event);
    }
}
