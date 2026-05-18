package org.galaxy.beyond.api.system.rogue.phase.player;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

public class PlayerSpectatorPhase extends PlayerPhase {
    public PlayerSpectatorPhase() { super(BeyondPhaseInit.P_SPECTATOR); }

    @Override public void enter(ServerLevel level, IRogueContext ctx) {}
    @Override public void tick(ServerLevel level, IRogueContext ctx) {}
    @Override public void exit(ServerLevel level, IRogueContext ctx) {}
}
