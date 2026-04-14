package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.init.BeyondSceneTypes;
import com.pz.beyond.api.system.node.EncounterType;

/**
 * 普通怪物（橙色 + 资源类型）
 */
public class NormalMonsterEncounter extends EncounterType {
    public NormalMonsterEncounter() {
        super(BeyondSceneTypes.HARVEST);
    }
}
