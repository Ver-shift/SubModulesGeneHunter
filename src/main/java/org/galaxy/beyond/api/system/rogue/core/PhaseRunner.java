package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;

public class PhaseRunner {

    private final Pipeline pipeline;
    private StepNode current;
    private final RogueContext ctx;
    private boolean entered;
    private boolean justEntered;

    public PhaseRunner(Pipeline pipeline, RogueContext ctx, RogueState initialState) {
        this.pipeline = pipeline;
        this.ctx = ctx;
        this.current = pipeline.lookup(initialState);
        if (this.current == null) {
            this.current = pipeline.first();
        }
    }

    public void tick(ServerLevel level) {
        if (!entered) {
            current.phase.enter(level, ctx);
            entered = true;
            justEntered = true;
        }

        if (!justEntered && current.transition != null && current.transition.isSatisfied(level, ctx)) {
            current.phase.exit(level, ctx);
            current.transition.onTransition(level, ctx);

            StepNode next = pipeline.advance(current);
            if (next != null && next != current) {
                current = next;
                ctx.setState(level, current.state);
            }

            current.phase.enter(level, ctx);
            justEntered = true;
        }

        current.phase.tick(level, ctx);
        justEntered = false;
    }

    public RogueState currentState() {
        return current.state;
    }

    public void forceState(ServerLevel level, RogueState state) {
        StepNode target = pipeline.lookup(state);
        if (target != null) {
            current.phase.exit(level, ctx);
            current = target;
            ctx.setState(level, state);
            current.phase.enter(level, ctx);
            entered = true;
            justEntered = true;
        }
    }
}
