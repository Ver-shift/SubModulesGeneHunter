package org.galaxy.beyond.api.system.rogue.core;

import javax.annotation.Nullable;

class StepNode {
    final RogueState state;
    final IRoguePhase phase;
    final ITransition transition;
    @Nullable StepNode next;
    boolean subEntry;
    @Nullable Pipeline subPipeline;
    @Nullable RogueState subBackTo;

    StepNode(RogueState state, IRoguePhase phase, ITransition transition) {
        this.state = state;
        this.phase = phase;
        this.transition = transition;
    }
}
