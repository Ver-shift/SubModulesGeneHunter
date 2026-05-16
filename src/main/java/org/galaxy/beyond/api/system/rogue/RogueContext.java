package org.galaxy.beyond.api.system.rogue;

import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.*;

import java.util.List;

public class RogueContext {

    private final IRogueNodeManager nodeManager;
    @Getter
    private final IPlayerRougeManager playerManager;
    @Getter
    private final ISceneManager sceneManager;
    @Getter
    private final ProgressManager progressManager;

    public RogueContext(IRogueNodeManager nodeManager, IPlayerRougeManager playerManager,
                        ISceneManager sceneManager, ProgressManager progressManager) {
        this.nodeManager = nodeManager;
        this.playerManager = playerManager;
        this.sceneManager = sceneManager;
        this.progressManager = progressManager;
    }

    public RogueData data(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(level).getRogueData();
    }

    public RogueState currentState(ServerLevel level) {
        return data(level).getRogueState();
    }

    public void setState(ServerLevel level, RogueState state) {
        data(level).setRogueState(state);
    }

    public RogueNodeData nodeData(ServerLevel level) {
        return data(level).getRogueNodeData();
    }

    public void setNodeData(ServerLevel level, RogueNodeData nodeData) {
        data(level).setRogueNodeData(nodeData);
    }

    public List<ServerPlayer> inGamePlayers(ServerLevel level) {
        return data(level).getInGamePlayers();
    }

    public boolean allPlayersMatch(ServerLevel level, PlayerRogueState state) {
        var players = inGamePlayers(level);
        if (players.isEmpty()) return false;
        for (var p : players) {
            if (playerManager.getState(p) != state) return false;
        }
        return true;
    }

    public boolean anyPlayerMatch(ServerLevel level, PlayerRogueState state) {
        for (var p : inGamePlayers(level)) {
            if (playerManager.getState(p) == state) return true;
        }
        return false;
    }

    public void setAllPlayerState(ServerLevel level, PlayerRogueState state) {
        for (var p : inGamePlayers(level)) {
            playerManager.setState(p, state);
        }
    }

    public long getGameSeed(ServerLevel level) {
        return data(level).getGameSeed();
    }

    public void setGameSeed(ServerLevel level, long seed) {
        data(level).setGameSeed(seed);
    }

    public IRogueNodeManager node() {
        return nodeManager;
    }
}
