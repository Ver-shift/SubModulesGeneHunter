package org.galaxy.beyond.api.system.rogue;

import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.IPlayerRougeManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueStateManager;
import org.galaxy.beyond.api.system.rogue.player.PlayerRougeManager;

import java.util.List;

public class RogueManager implements IRogueManager {

    @Getter
    private final IRogueStateManager rogueStateManager = new RogueStateManager();
    @Getter
    private final IPlayerRougeManager playerRougeManager = new PlayerRougeManager();

    @Override
    public void setRogueLevel(ResourceKey<Level> level) {
        BeyondAPI.getGlobalData(Beyond.SERVER).setRougeLevel(level);
    }

    // ======================== tick 总线 ========================

    @Override
    public void tick(ServerLevel level) {
        rogueStateManager.tick(level);
        List<ServerPlayer> playerList = BeyondAPI.getBeyondLevelData(level).getRogueData().getInGamePlayers();
        for (ServerPlayer player : playerList) {
            playerRougeManager.tick(player);
        }
    }




}
