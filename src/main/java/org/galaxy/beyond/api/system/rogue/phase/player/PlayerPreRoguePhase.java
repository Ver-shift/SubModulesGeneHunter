package org.galaxy.beyond.api.system.rogue.phase.player;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

public class PlayerPreRoguePhase extends PlayerPhase {
    public PlayerPreRoguePhase() { super(BeyondPhaseInit.P_PRE_ROGUE); }

    @Override public void enter(ServerLevel level, IRogueContext ctx) {}
    @Override public void tick(ServerLevel level, IRogueContext ctx) {}
    @Override public void exit(ServerLevel level, IRogueContext ctx) {}
}
