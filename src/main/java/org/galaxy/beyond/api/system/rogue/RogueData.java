package org.galaxy.beyond.api.system.rogue;

import lombok.Data;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

@Data
public class RogueData {

    private ResourceKey<Level> rogueLevel = Level.OVERWORLD;
    //参与在游戏里面的玩家
    private List<ServerPlayer> inGamePlayers = new ArrayList<>();
    //游戏状态
    private RogueState rogueState = RogueState.LOBBY;
    //肉鸽节点数据
    private RogueNodeData rogueNodeData;
    //当前副本（一次冒险只有一个）
    private ProgressType progressType;
    //本局种子，开局时从进度种子克隆而来，保证整局体验不变
    private long gameSeed;


}
