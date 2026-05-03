package org.galaxy.beyond.api.system.rogue.player;

public enum PlayerRogueState {
    /**
     * 玩家在安全区内部
     */
    IN_SAFE_ZONE,

    /**
     * 玩家准备好启动了，需要所有玩家都ready
     */
    READY_ROGUE,

    /**
     * 玩家正在大世界，没进入节点
     */
    ON_ROGUE,

    /**
     * 玩家正在节点内部，但是并没有触发任何事件。
     */
    IN_NODE,

    /**
     * 玩家在节点内部，准备启动，ready，每个玩家ready 才能启动
     */
    READY_NODE,

    /**
     * 玩家死亡，进行漂浮状态。
     */
    DEAD,

    /**
     * 中间态过渡，玩家经过任何的状态都会有，通过总线自动确认下一个状态。
     */
    EMPTY;


    public boolean inNode(){
        return this == IN_NODE || this == READY_NODE;
    }
}
