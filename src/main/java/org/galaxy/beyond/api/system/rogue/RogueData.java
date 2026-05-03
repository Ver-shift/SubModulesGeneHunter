package org.galaxy.beyond.api.system.rogue;

import lombok.Data;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
@Data
public class RogueData {

    //参与在游戏里面的玩家
    private List<ServerPlayer> inGamePlayers;
    private RogueState rogueState;


}
