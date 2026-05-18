package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.IRogueContext;

/**
 * 肉鸽全局 Phase 运行器 —— 驱动 {@link RoguePhase} 的生命周期。
 */
public class PhaseRunner {

    private final IRogueContext ctx;
    private RoguePhase current;
    private boolean entered;

    public PhaseRunner(IRogueContext ctx) {
        this.ctx = ctx;
    }

    public void tick(ServerLevel level) {
        RoguePhase phase = ctx.getPhase(level);

        if (phase != current) {
            entered = false;
            current = phase;
        }

        if (!entered) {
            if (phase != null) phase.enter(level, ctx);
            entered = true;
        }

        if (phase != null) phase.tick(level, ctx);
    }

    public void forceState(ServerLevel level, RoguePhase target) {
        RoguePhase cur = ctx.getPhase(level);
        if (target.equals(cur) && entered) return;

        if (entered && cur != null) cur.exit(level, ctx);

        ctx.setPhase(level, target);
        current = target;
        entered = false;

        if (target != null) {
            target.enter(level, ctx);
            entered = true;
        }
    }
}
