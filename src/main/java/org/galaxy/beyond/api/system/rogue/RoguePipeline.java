package org.galaxy.beyond.api.system.rogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.*;
import org.galaxy.beyond.api.system.rogue.definition.DefinitionManager;
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
                .step(RogueState.PRE_ROGUE, new PreRoguePhase(), allPlayersReadyTransition())
                .step(RogueState.ROGUE_INIT, new RogueInitPhase(), Transitions.immediately())
                .step(RogueState.ON_PROGRESS, new OnProgressPhase(), Transitions.anyPlayer(PlayerRogueState.PRE_NODE))
                .sub(nodeSubPipeline(), RogueState.ON_PROGRESS)
                .step(RogueState.ROGUE_PROGRESS_FINISH, new RogueProgressFinishPhase(), Transitions.allPlayers(PlayerRogueState.PROGRESS_FINISH))
                .build();
    }

    /** 全员 PRE_ROGUE 后的初始化：组装 ProgressType 并播报 */
    private static ITransition allPlayersReadyTransition() {
        return new ITransition() {
            @Override
            public boolean isSatisfied(ServerLevel level, RogueContext ctx) {
                return ctx.allPlayersMatch(level, PlayerRogueState.PRE_ROGUE);
            }

            @Override
            public void onTransition(ServerLevel level, RogueContext ctx) {
                var globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
                var progressId = globalData.getRogueConfig().getCurrentProgress();
                if (progressId == null) {
                    level.getServer().sendSystemMessage(Component.translatable("beyond.rogue.start.no_progress"));
                    return;
                }
                var definition = globalData.getRogueDefinition().getRogueProgress().get(progressId);
                if (definition == null) {
                    level.getServer().sendSystemMessage(Component.translatable("beyond.rogue.start.progress_not_found", progressId.toString()));
                    return;
                }

                ProgressType progressType = new ProgressType(progressId);
                BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData().setProgressType(progressType);

                var defManager = new DefinitionManager();
                var scenes = defManager.resolveScenes(level);
                progressType.setScenes(scenes);

                level.getServer().sendSystemMessage(Component.translatable("beyond.rogue.start", progressId.toString()));
            }
        };
    }
}
