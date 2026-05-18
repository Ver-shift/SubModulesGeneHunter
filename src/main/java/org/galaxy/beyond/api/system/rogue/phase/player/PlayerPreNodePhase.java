package org.galaxy.beyond.api.system.rogue.phase.player;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

public class PlayerPreNodePhase extends PlayerPhase {
    public PlayerPreNodePhase() { super(BeyondPhaseInit.P_PRE_NODE); }

    @Override public void enter(ServerLevel level, IRogueContext ctx) {}
    @Override public void tick(ServerLevel level, IRogueContext ctx) {}
    @Override public void exit(ServerLevel level, IRogueContext ctx) {}
}
