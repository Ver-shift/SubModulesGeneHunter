package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.core.PhaseRunner;

public class RogueManager {

    private final RogueContext context = new RogueContext();
    private final PhaseRunner phaseRunner = new PhaseRunner(context);

    public RogueContext getContext() {
        return context;
    }

    public PhaseRunner getPhaseRunner() {
        return phaseRunner;
    }

    public void tick(ServerLevel level) {
        phaseRunner.tick(level);
    }
}
