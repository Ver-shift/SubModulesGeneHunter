package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RogueContext implements IRogueContext {

    @Override
    public RoguePhase getPhase(ServerLevel level) {
        return getRogueData(level).getPhase();
    }

    @Override
    public void setPhase(ServerLevel level, RoguePhase phase) {
        getRogueData(level).setPhase(phase);
        BeyondAPI.syncGlobalData(level);
    }

    @Override
    public void setPhase(ServerLevel level, Supplier<RoguePhase> supplier) {
        setPhase(level, supplier.get());
    }

    @Override
    public PlayerPhase getPlayerPhase(ServerPlayer player) {
        return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
    }

    @Override
    public void setPlayerPhase(ServerPlayer player, PlayerPhase phase) {
        BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(phase);
        BeyondAPI.syncPlayerData(player);
    }

    @Override
    public void setPlayerPhase(ServerPlayer player, Supplier<PlayerPhase> supplier) {
        setPlayerPhase(player, supplier.get());
    }

    @Override
    public void setAllPlayerPhase(ServerLevel level, PlayerPhase phase) {
        for (ServerPlayer p : playersInRogue(level)) {
            setPlayerPhase(p, phase);
        }
    }

    @Override
    public void setAllPlayerPhase(ServerLevel level, Supplier<PlayerPhase> supplier) {
        setAllPlayerPhase(level, supplier.get());
    }

    @Override
    public List<ServerPlayer> playersInRogue(ServerLevel level) {
        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) continue;
            if (getPlayerPhase(player) != PlayerPhase.LOBBY) result.add(player);
        }
        return result;
    }

    @Override
    public boolean allPlayersMatchPhase(ServerLevel level, PlayerPhase phase) {
        var players = playersInRogue(level);
        if (players.isEmpty()) return false;
        for (ServerPlayer player : players) {
            if (getPlayerPhase(player) != phase) return false;
        }
        return true;
    }

    @Override
    public RogueData getRogueData(ServerLevel level) {
        return BeyondAPI.getRogueData(level);
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
        BeyondAPI.syncGlobalData(level);
    }
}
