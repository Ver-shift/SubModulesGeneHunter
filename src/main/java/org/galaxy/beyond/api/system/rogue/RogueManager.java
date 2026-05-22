package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.core.PhaseRunner;

public class RogueManager {

    private final IRogueContext context;
    private final PhaseRunner phaseRunner;

    public RogueManager(IRogueContext context) {
        this.context = context;
        this.phaseRunner = new PhaseRunner(context);
    }

    public IRogueContext getContext() {
        return context;
    }

    public PhaseRunner getPhaseRunner() {
        return phaseRunner;
    }

    public void tick(ServerLevel level) {
        phaseRunner.tick(level);
    }
}
