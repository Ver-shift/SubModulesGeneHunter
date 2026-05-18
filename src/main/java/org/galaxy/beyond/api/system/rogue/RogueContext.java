package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

import java.util.*;

public class RogueContext implements IRogueContext {

    @Override
    public RoguePhase getPhase(ServerLevel level) {
        return getRogueData(level).getPhase();
    }

    @Override
    public void setPhase(ServerLevel level, RoguePhase phase) {
        getRogueData(level).setPhase(phase);
    }

    @Override
    public PlayerPhase getPlayerPhase(ServerPlayer player) {
        return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
    }

    @Override
    public void setPlayerPhase(ServerPlayer player, PlayerPhase phase) {
        BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(phase);
    }

    @Override
    public void setAllPlayerPhase(ServerLevel level, PlayerPhase phase) {
        for (ServerPlayer p : playersInRogue(level)) {
            setPlayerPhase(p, phase);
        }
    }

    @Override
    public List<ServerPlayer> playersInRogue(ServerLevel level) {
        var cfg = BeyondAPI.getGlobalData(level).getRogueConfig();
        var playerList = level.getServer().getPlayerList();
        List<ServerPlayer> result = new ArrayList<>();
        for (UUID id : cfg.getRoguePlayerIds()) {
            ServerPlayer p = playerList.getPlayer(id);
            if (p != null) result.add(p);
        }
        return result;
    }

    @Override
    public boolean allPlayersMatchPhase(ServerLevel level, PlayerPhase phase) {
        var ids = BeyondAPI.getGlobalData(level).getRogueConfig().getRoguePlayerIds();
        if (ids.isEmpty()) return false;
        for (UUID uuid : ids) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(uuid);
            if (p == null || !getPlayerPhase(p).equals(phase)) return false;
        }
        return true;
    }

    @Override
    public RogueData getRogueData(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(level).getRogueData();
    }

    @Override
    public RogueNodeData getRogueNodeData(ServerLevel level) {
        return getRogueData(level).getRogueNodeData();
    }

    @Override
    public long getGameSeed(ServerLevel level) {
        return getRogueData(level).getGameSeed();
    }

    @Override
    public void setGameSeed(ServerLevel level, long seed) {
        getRogueData(level).setGameSeed(seed);
    }
}
