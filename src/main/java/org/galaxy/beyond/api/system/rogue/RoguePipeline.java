package org.galaxy.beyond.api.system.rogue;

import org.galaxy.beyond.api.system.rogue.core.*;
import org.galaxy.beyond.api.system.rogue.phase.*;

/**
 * 肉鸽完整流程声明。节点子流程简化为 PRE_NODE → PRE_EVENT → ON_EVENT。
 */
public final class RoguePipeline {

    private RoguePipeline() {}

    private static Pipeline nodeSubPipeline() {
        return Pipeline.builder()
                .start(RogueState.PRE_NODE)
                .step(RogueState.PRE_EVENT,  new PreEventPhase(),  Transitions.immediately())
                .step(RogueState.ON_EVENT,   new OnEventPhase(),   Transitions.immediately())
                .build();
    }

    public static Pipeline build() {
        return Pipeline.builder()
                .start(RogueState.LOBBY)
                .step(RogueState.PRE_ROGUE,               new PreRoguePhase(),          Transitions.allPlayers(PlayerRogueState.PRE_ROGUE))
                .step(RogueState.ROGUE_INIT,              new RogueInitPhase(),         Transitions.immediately())
                .step(RogueState.ON_PROGRESS,             new OnProgressPhase(),        Transitions.anyPlayer(PlayerRogueState.PRE_NODE))
                .sub(nodeSubPipeline(), RogueState.ON_PROGRESS)
                .step(RogueState.ROGUE_PROGRESS_FINISH,   new RogueProgressFinishPhase(), Transitions.allPlayers(PlayerRogueState.PROGRESS_FINISH))
                .build();
    }
}
