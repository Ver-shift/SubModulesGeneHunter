package org.galaxy.beyond.api.system.rogue.phase.node;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;

public class NodePreEventPhase extends NodePhase {
    public NodePreEventPhase() { super(BeyondPhaseInit.N_PRE_EVENT); }

    @Override public void enter(ServerLevel level, IRogueContext ctx) {}
    @Override public void tick(ServerLevel level, IRogueContext ctx) {}
    @Override public void exit(ServerLevel level, IRogueContext ctx) {}
}
