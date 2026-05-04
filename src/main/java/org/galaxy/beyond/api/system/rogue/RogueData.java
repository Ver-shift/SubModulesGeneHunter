package org.galaxy.beyond.api.system.rogue;

import lombok.Data;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
@Data
public class RogueData {

    //参与在游戏里面的玩家
    private List<ServerPlayer> inGamePlayers;
    //游戏状态
    private RogueState rogueState;
    //肉鸽节点数据
    private RogueNodeData rogueNodeData;
    //当前关卡数据
    private ProgressType progressType;
}
