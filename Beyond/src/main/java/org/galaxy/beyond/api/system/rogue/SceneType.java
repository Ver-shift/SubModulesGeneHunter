package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;

/**
 * 单个进度类型，与节点颜色组合成遭遇战类型。
 */
public enum SceneType implements IPersistedSerializable {

    /** 资源事件：用于获取资源的房间，可能是战斗或小游戏 */
    HARVEST,
    /** 修正事件：用于调整对局节奏，恢复状态或强化自身 */
    REPOSE,
    /** BOSS 挑战事件：必定出现的 boss 节点，包含商店 */
    CLIMAX,
    /** 空类型 */
    EMPTY;

    @Persisted
    private final String id;

    SceneType() {
        this.id = name();
    }

    @SuppressWarnings("unused")
    private static SceneType valueOfPersisted(String id) {
        try { return valueOf(id); } catch (IllegalArgumentException e) { return EMPTY; }
    }
}
