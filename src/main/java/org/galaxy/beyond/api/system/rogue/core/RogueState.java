package org.galaxy.beyond.api.system.rogue.core;

public enum RogueState {
    /**
     * 局外养成时间，所有玩家在安全区
     */
    LOBBY,
    /**
     * 至少一个玩家跨出安全区边界，等待全部ready
     */
    PRE_ROGUE,

    ROGUE_INIT,
    /**
     * 游戏正在运行，所有玩家在大世界
     */
    ON_PROGRESS,
    //=========================这几个都是节点方法，会同步改变节点的state=================================
    /**
     * 至少一名玩家触发了节点方块
     */
    PRE_NODE,

    /**
     * 节点进行初始化，如果有多个玩家在不同的地方进行了节点方块触发，那么就抽取并且进行玩家传送。
     */
    NODE_INIT,

    /**
     * 只要有一个玩家转为PreEvent状态。就是触发ROGUE_PRE_EVENT状态，等待所有玩家准备好进入OnEventTask
     */
    ROGUE_PRE_EVENT,

    /**
     * 节点事件触发器
     */
    ON_EVENT_TASK,

    /**
     * 节点结束了会触发的逻辑，执行完了过后会转到ON_PROGRESS或者REWARD。
     */
    NODE_FINISH,

    //========================================================================
    /**
     * 游戏结束
     */
    ROGUE_PROGRESS_FINISH,

    EMPTY;

}
