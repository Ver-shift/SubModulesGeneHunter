package org.galaxy.beyond.api.system.rogue;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueState;

import java.util.List;

public class RogueManager implements IRogueManager {



    @Override
    public void setRogueLevel(ResourceKey<Level> level) {

    }

    @Override
    public void tick(ServerLevel level) {
        // TODO: 框架占位
        
        
    }

    @Override
    public void tryStratRogue(ServerLevel level) {
        // TODO: 框架占位
    }

    @Override
    public void startRogue(ServerLevel level) {

    }

    @Override
    public void tryStratNode(ServerLevel level) {
        // TODO: 框架占位
    }

    @Override
    public void tryFinishRogue(ServerLevel level) {
        // TODO: 框架占位
    }

    @Override
    public boolean isPlayerAllReady(ServerLevel level, PlayerRogueState playerRogueState) {
        List<ServerPlayer> players = BeyondAPI.getBeyondLevelData(level).getRogueData().getInGamePlayers();
        for (ServerPlayer player : players) {
            PlayerRogueState state = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getState();
            if (state != playerRogueState) {
                return false;
            }
        }
        return !players.isEmpty();
    }
}
