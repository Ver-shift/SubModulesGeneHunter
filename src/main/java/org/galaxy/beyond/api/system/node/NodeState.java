package org.galaxy.beyond.api.system.node;

import net.minecraft.util.StringRepresentable;

public enum NodeState {
    /**
     * 锁定状态，
     */
    LOCKED,
    /**
     * 当有一个但是不为全部游戏玩家启动数据
     */
    PRE_START,
    /**
     * 已经启动，正在运行事件中
     */
    ON_EVENT,
    /**
     * 节点事件全部完成，等待玩家做一些操作。
     */
    PRE_FINISH,
    /**
     * 节点完全解锁，不能做任何的事情了
     */
    UNLOCKED;

    public boolean isUnlocked() {
        return this.equals(UNLOCKED);
    }
}
