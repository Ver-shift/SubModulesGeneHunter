package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.Phase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

/**
 * 肉鸽初始化 Cap。
 * <p>
 * 在 {@code ROGUE_INIT} phase 进入时执行关卡初始化：清空上局数据、生成种子、
 * 将 ProgressDefinition 解析为运行时的 {@link ProgressType} 并推进到 {@code ROGUE_ON_PROGRESS}。
 */
public class RogueInitCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("rogue_init");

    public RogueInitCap() { super(ID); }

    @Override
    public void phaseEnter(ServerLevel level, Phase from, Phase to, IRogueContext ctx) {
        if (to != RoguePhase.INIT) return;

        var globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
        var rogueData = ctx.getRogueData(level);
        var progressId = rogueData.getProgressId();

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

        rogueData.resetProgressState();

        long seed = ctx.getGameSeed(level);
        if (seed == 0) {
            seed = level.getRandom().nextLong();
            ctx.setGameSeed(level, seed);
        }

        var progressType = rogueData.getProgressType();
        progressType.setId(progressId);
        progressType.setActive(true);
        progressType.setScenes(Beyond.MANAGER.getDefinitionManager().resolveScenes(level));
        BeyondAPI.syncGlobalData(level);

        level.getServer().getPlayerList()
                .broadcastSystemMessage(Component.translatable("beyond.rogue.start", progressId.toString()), false);

        ctx.setPhase(level, RoguePhase.ON_PROGRESS);
    }
}
