package org.galaxy.beyond.api.system.rogue.core;

/**
 * 进度事件类型 —— 决定单个进度节点的性质。
 */
public enum ProgressEventType {
    /** 资源事件：战斗/小游戏，提升综合能力 */
    RESOURCE,
    /** 修正事件：恢复状态或强化自身 */
    CORRECTION,
    /** Boss 挑战 */
    BOSS
}
