package org.galaxy.beyond.api.system.node.core;

/**
 * 节点状态是真正执行逻辑的地方，其状态完全通过RogueState控制。
 */
public enum NodeState {

    /**
     * 锁定
     */
    LOCKED,
    /**
     * 当RogueState 为PRE_NODE时变成。
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

    /**
     * 解锁
     */
    UNLOCKED,

}
