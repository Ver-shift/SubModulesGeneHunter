package org.galaxy.beyond.api.system.rogue.phase.player;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

public class PlayerProgressFinishPhase extends PlayerPhase {
    public PlayerProgressFinishPhase() { super(BeyondPhaseInit.P_PROGRESS_FINISH); }

    @Override public void enter(ServerLevel level, IRogueContext ctx) {}
    @Override public void tick(ServerLevel level, IRogueContext ctx) {}
    @Override public void exit(ServerLevel level, IRogueContext ctx) {}
}
