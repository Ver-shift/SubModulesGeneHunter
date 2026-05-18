package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.RogueState;

public class OnProgressPhase implements IRoguePhase {

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        int current = ctx.getProgressManager().getCurrentProgressIndex(level);
        int total = ctx.getProgressManager().getTotalProgress(level);
        if (total > 0 && current >= total) {
            ctx.forceTo(level, RogueState.ROGUE_PROGRESS_FINISH);
        }
    }
}
