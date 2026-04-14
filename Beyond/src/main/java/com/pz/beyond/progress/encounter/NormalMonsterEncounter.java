package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.progress.SceneType;

/**
 * 普通怪物（橙色 + 资源类型）
 */
public class NormalMonsterEncounter extends EncounterType {
    public NormalMonsterEncounter() {
        super(SceneType.HARVEST);
    }
}
