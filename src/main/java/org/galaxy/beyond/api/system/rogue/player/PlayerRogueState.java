package org.galaxy.beyond.api.system.rogue.player;

public enum PlayerRogueState {
    /**
     * 玩家在安全区内部
     */
    IN_SAFE_ZONE,

    /**
     * 跨出安全区边界，等待所有玩家ready
     */
    READY_ROGUE,

    /**
     * 玩家正在大世界，没进入节点
     */
    ON_ROGUE,

    /**
     * 进入节点区域，未触发事件
     */
    IN_NODE,

    /**
     * 右键节点方块，等待所有玩家ready，启动节点第一个事件
     */
    READY_NODE,

    /**
     * 节点事件正在执行中
     */
    IN_NODE_EVENT,

    /**
     * 当前事件完成，等待所有玩家ready，触发下一个事件
     */
    READY_NEXT,

    /**
     * 玩家死亡，还有复活次数
     */
    DEAD,

    /**
     * 复活次数归零，观战等待结算
     */
    SPECTATING,

    /**
     * 中间态过渡，由tick自动确定下一个状态
     */
    EMPTY;

    // ===== 分组判断 =====

    public boolean isInSafeZone() {
        return this == IN_SAFE_ZONE;
    }

    public boolean isInGame() {
        return this == READY_ROGUE || this == ON_ROGUE || isInNode();
    }

    public boolean isInNode() {
        return this == IN_NODE || this == READY_NODE
            || this == IN_NODE_EVENT || this == READY_NEXT;
    }

    public boolean isDead() {
        return this == DEAD || this == SPECTATING;
    }

    // ===== 状态转换合法性 =====

    public boolean canTransitionTo(PlayerRogueState target) {
        return switch (this) {
            case IN_SAFE_ZONE  -> target == READY_ROGUE || target == ON_ROGUE;
            case READY_ROGUE   -> target == ON_ROGUE || target == IN_SAFE_ZONE;
            case ON_ROGUE      -> target == IN_NODE || target == DEAD || target == IN_SAFE_ZONE;
            case IN_NODE       -> target == READY_NODE || target == ON_ROGUE || target == DEAD;
            case READY_NODE    -> target == IN_NODE_EVENT || target == ON_ROGUE;
            case IN_NODE_EVENT -> target == READY_NEXT    // 单个事件完成，等待下一事件
                               || target == ON_ROGUE      // EventTask全部完成
                               || target == DEAD;         // 事件中死亡
            case READY_NEXT    -> target == IN_NODE_EVENT  // 全部ready，触发下一事件
                               || target == ON_ROGUE;      // 放弃节点
            case DEAD          -> target == ON_ROGUE || target == SPECTATING;
            case SPECTATING    -> target == IN_SAFE_ZONE;
            case EMPTY         -> true;
        };
    }
}
