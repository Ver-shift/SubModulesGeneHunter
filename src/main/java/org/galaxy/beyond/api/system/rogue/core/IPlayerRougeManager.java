package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerPlayer;

public interface IPlayerRougeManager {

    /**
     * 通过tick自动判断玩家的state
     * @param player
     */
    void tick(ServerPlayer player);

    /**
     * 尝试加入肉鸽系统，都是为了teacon，不敢随意拉屎
     * @param player
     */
    void intoRogue(ServerPlayer player);

    /**
     * 离开肉鸽系统，离开维度的时候固定调用。
     * @param player
     */
    void levelRogue(ServerPlayer player);

    /**
     * 玩家加入当前的关卡调用，
     * 玩家在初始态加入
     * 玩家中途加入，直接复制一套其他玩家相同的装备。
     */
    void intoProgress(ServerPlayer player);
}
