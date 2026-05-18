package org.galaxy.beyond.api.system.rogue.phase.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

public class RogueOnProgressPhase extends RoguePhase {
    public RogueOnProgressPhase() { super(BeyondPhaseInit.R_ON_PROGRESS); }

    @Override public void enter(ServerLevel level, IRogueContext ctx) {}
    @Override public void tick(ServerLevel level, IRogueContext ctx) {}
    @Override public void exit(ServerLevel level, IRogueContext ctx) {}
}
