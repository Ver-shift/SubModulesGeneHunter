package com.pz.beyond.api.system.node;

public enum NodeState {
    /**
     * 锁定中，未进入
     */
    LOCKED,
    /**
     * 玩家进入节点，已 roll 出事件，等待触发
     */
    READY,
    /**
     * 正在执行事件列表中
     */
    ON_EVENT,
    /**
     * 事件全部完成，节点显示为蓝色
     */
    COMPLETED
}
