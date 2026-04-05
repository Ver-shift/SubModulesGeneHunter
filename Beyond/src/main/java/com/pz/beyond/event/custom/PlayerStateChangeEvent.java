package com.pz.beyond.event.custom;

import com.pz.beyond.data.PlayerStateData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 玩家状态变更事件。
 * <p>
 * 当玩家的状态发生改变时触发此事件，包含玩家对象、变更前状态和变更后状态。
 * <p>
 * 该事件可被取消。
 */
public class PlayerStateChangeEvent extends Event implements ICancellableEvent {
    /**
     * 发生状态变更的玩家实体。
     */
    private final Player player;
    /**
     * 变更前的玩家状态。
     */
    private final PlayerStateData.PlayerState fromState;
    /**
     * 变更后的玩家状态。
     */
    private final PlayerStateData.PlayerState toState;

    /**
     * 构造玩家状态变更事件。
     *
     * @param player    发生状态变更的玩家实体
     * @param fromState 变更前的玩家状态
     * @param toState   变更后的玩家状态
     */
    public PlayerStateChangeEvent(Player player, PlayerStateData.PlayerState fromState, PlayerStateData.PlayerState toState) {
        this.player = player;
        this.fromState = fromState;
        this.toState = toState;
    }

    /**
     * 获取发生状态变更的玩家实体。
     *
     * @return 玩家实体
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取变更前的玩家状态。
     *
     * @return 变更前的状态
     */
    public PlayerStateData.PlayerState getFromState() {
        return fromState;
    }

    /**
     * 获取变更后的玩家状态。
     *
     * @return 变更后的状态
     */
    public PlayerStateData.PlayerState getToState() {
        return toState;
    }

}
