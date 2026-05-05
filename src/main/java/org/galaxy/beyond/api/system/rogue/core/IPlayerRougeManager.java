package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueState;
import org.galaxy.beyond.api.system.zone.ZoneType;

public interface IPlayerRougeManager {

    /**
     * 通过tick自动判断玩家的state
     */
    void tick(ServerPlayer player);

    void playerChangeZone(ServerPlayer player, ZoneType from,ZoneType to);
    /**
     * 尝试加入肉鸽系统
     */
    void intoRogue(ServerPlayer player);

    /**
     * 离开肉鸽系统，离开维度时固定调用
     */
    void leaveRogue(ServerPlayer player);

    /**
     * 玩家加入当前副本
     * 玩家在初始态加入
     * 玩家中途加入，直接复制一套其他玩家相同的装备
     */
    void intoProgress(ServerPlayer player);

    /**
     * 设置玩家状态，发布 PlayerRogueStateChangeEvent
     */
    void setState(ServerPlayer player, PlayerRogueState newState);

    /**
     * 获取玩家当前状态
     */
    PlayerRogueState getState(ServerPlayer player);


    void playerUseLootBag(ServerPlayer player);

    boolean isInGame(ServerPlayer player);

}
