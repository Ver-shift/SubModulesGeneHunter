package org.galaxy.beyond.api.system.rogue;

import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.*;
import org.galaxy.beyond.api.system.rogue.player.PlayerRougeManager;

import java.util.List;

public class RogueManager implements IRogueManager {

    @Getter
    private final IPlayerRougeManager playerRougeManager;
    @Getter
    private final IRogueNodeManager rogueNodeManager;
    @Getter
    private final IRogueStateManager rogueStateManager;
    @Getter
    private final ISceneManager sceneManager;
    @Getter
    private final ProgressManager progressManager;

    private final RogueContext ctx;
    private final PhaseRunner runner;

    public RogueManager() {
        playerRougeManager = new PlayerRougeManager(this);
        rogueNodeManager = new RogueNodeManager(this);
        sceneManager = new SceneManager();
        progressManager = new ProgressManager();

        // 注册默认行动路线
        progressManager.registerRoute(ProgressManager.createDefaultRoute());
        progressManager.setCurrentRoute(progressManager.getRoute(Beyond.asResource("default").toString()));

        ctx = new RogueContext(rogueNodeManager, playerRougeManager, sceneManager, progressManager);
        runner = new PhaseRunner(RoguePipeline.build(), ctx, RogueState.LOBBY);
        rogueStateManager = new RogueStateManager(runner, ctx);
    }

    @Override
    public void setRogueLevel(ResourceKey<Level> dimension) {
        BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).setRougeLevel(dimension);
    }

    @Override
    public void tick(ServerLevel level) {
        runner.tick(level);
        List<ServerPlayer> playerList = BeyondAPI.getBeyondDimensionData(level).getRogueData().getInGamePlayers();
        for (ServerPlayer player : playerList) {
            playerRougeManager.tick(player);
        }
    }

    @Override
    public IRogueStateManager getRogueStateManager() {
        return rogueStateManager;
    }
}
