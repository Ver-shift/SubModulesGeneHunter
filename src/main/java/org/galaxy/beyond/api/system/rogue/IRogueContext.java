package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

import java.util.List;

/**
 * Phase 运行时上下文接口 —— 直接操作 Phase 实例。
 */
public interface IRogueContext {

    RoguePhase getPhase(ServerLevel level);

    void setPhase(ServerLevel level, RoguePhase phase);

    PlayerPhase getPlayerPhase(ServerPlayer player);

    void setPlayerPhase(ServerPlayer player, PlayerPhase phase);

    void setAllPlayerPhase(ServerLevel level, PlayerPhase phase);

    List<ServerPlayer> playersInRogue(ServerLevel level);

    boolean allPlayersMatchPhase(ServerLevel level, PlayerPhase phase);

    RogueData getRogueData(ServerLevel level);

    RogueNodeData getRogueNodeData(ServerLevel level);

    long getGameSeed(ServerLevel level);

    void setGameSeed(ServerLevel level, long seed);
}
