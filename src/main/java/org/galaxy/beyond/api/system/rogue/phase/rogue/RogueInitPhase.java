package org.galaxy.beyond.api.system.rogue.phase.rogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.ProgressType;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

/**
 * ROGUE_INIT Phase：定义 → ProgressType 转化 + 关卡初始化。
 */
public class RogueInitPhase extends RoguePhase {

    public RogueInitPhase() { super(BeyondPhaseInit.R_INIT); }

    @Override
    public void enter(ServerLevel level, IRogueContext ctx) {
        var globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
        var progressId = globalData.getRogueConfig().getCurrentProgress();

        if (progressId == null) {
            level.getServer().getPlayerList()
                    .broadcastSystemMessage(Component.translatable("beyond.rogue.start.no_progress"), false);
            return;
        }
        if (!globalData.getRogueDefinition().getRogueProgress().containsKey(progressId)) {
            level.getServer().getPlayerList()
                    .broadcastSystemMessage(Component.translatable("beyond.rogue.start.progress_not_found", progressId.toString()), false);
            return;
        }

        // 重置上一局数据
        var rogueData = ctx.getRogueData(level);
        rogueData.setRogueNodeData(null);
        rogueData.setProgressIndex(0);
        rogueData.getEncounterAssignments().clear();
        rogueData.getCompletedNodeChunks().clear();

        // 种子
        long seed = ctx.getGameSeed(level);
        if (seed == 0) {
            seed = level.getRandom().nextLong();
            ctx.setGameSeed(level, seed);
        }

        // 定义 → ProgressType 转化
        ProgressType progressType = new ProgressType(progressId);
        rogueData.setProgressType(progressType); // 先设 ID，resolveScenes 依赖它

        progressType.setScenes(Beyond.MANAGER.getDefinitionManager().resolveScenes(level));

        level.getServer().getPlayerList()
                .broadcastSystemMessage(Component.translatable("beyond.rogue.start", progressId.toString()), false);

        // 推进到 ON_PROGRESS
        ctx.setPhase(level, BeyondPhaseInit.ROGUE_ON_PROGRESS.get());
    }

    @Override public void tick(ServerLevel level, IRogueContext ctx) {}
    @Override public void exit(ServerLevel level, IRogueContext ctx) {}
}
