package org.galaxy.beyond.api.system.rogue;

public enum RogueState {
    /** 局外养成时间，游戏未开始 */
    LOBBY,

    /** 游戏准备时间，至少有一名玩家处于安全区外部 */
    READY,

    /**
     * 游戏正在运行，但是玩家不在节点
     */
    IN_PROGRESS,

    /** 游戏正在运行，并且正在节点里面 */
    IN_NODE,

    /** 游戏的结束时间，对玩家进行一些操作 */
    POST_GAME
}
