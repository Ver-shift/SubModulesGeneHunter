package org.galaxy.beyond.api.system.rogue.core;

public enum PlayerRogueState {
    /**
     * 玩家在安全区内部
     */
    LOBBY,

    /**
     * 跨出安全区边界，等待所有玩家ready
     */
    PRE_ROGUE,

    /**
     * 玩家正在肉鸽游戏里面，但是不在节点里面
     */
    ON_PROGRESS,

    /**
     * 玩家在节点内部，并且对节点方块进行交互
     */
    PRE_NODE,

    /**
     * 玩家准备下一个Event
     */
    PRE_EVENT,

    /**
     * 玩家正在触发节点事件内部
     */
    ON_EVENT,

    /**
     * 玩家死亡，进行判断的中间状态
     */
    SPECTATOR,

    /**
     * 玩家生命数耗尽。
     */
    DEAD,

    /**
     * 玩家进行结算，将背包里面的必要物品都进行清除
     */
    REWARD,

    /**
     * 玩家结束这一轮游戏
     */
    PROGRESS_FINISH,

    EMPTY;
    /**
     * 玩家的位置在安全区内
     * @return
     */
    public boolean isInSafeZone(){
        return this == LOBBY;
    }

    /**
     * 玩家的位置在安全区外面，在关卡内部
     * @return
     */
    public boolean isOnProgress(){
        return switch (this){
            case ON_PROGRESS,PRE_NODE,PRE_EVENT,ON_EVENT,SPECTATOR,DEAD,REWARD,PROGRESS_FINISH -> true;
            default -> false;
        };
    }
}













