package org.galaxy.beyond.api.system.rogue;

/***
 * 单个进度，与节点类型组合成遭遇战类型。
 */
public enum SceneType {
    /**
     * 资源事件：用于获取资源的房间，可能是战斗或小游戏
     */
    HARVEST,
    /**
     * 修正事件：用于调整对局节奏，恢复状态或强化自身
     */
    REPOSE,
    /**
     * BOSS挑战事件：必定出现的boss节点，包含商店
     */
    CLIMAX,

    /**
     * 空类型
     */
    EMPTY;


}
