package org.galaxy.beyond.api.system.rogue;

public enum RogueState {
    /**
     * 局外养成时间，所有玩家在安全区
     */
    LOBBY,

    /**
     * 至少一个玩家跨出安全区边界，等待全部ready
     */
    READY,

    /**
     * 游戏正在运行，所有玩家在大世界
     */
    IN_PROGRESS,

    /**
     * 至少一个玩家在节点内部
     */
    IN_NODE,

    /**
     * 完成胜利条件（可自定义）
     */
    POST_GAME,

    EMPTY;

    public boolean isGameActive() {
        return this == IN_PROGRESS || this == IN_NODE;
    }

    public boolean isGameEnding() {
        return this == POST_GAME;
    }
}
