package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.progress.SceneType;

/**
 * 精英怪挑战（红色 + 资源类型）
 */
public class EliteMonsterEncounter extends EncounterType {
    public EliteMonsterEncounter() {
        super(SceneType.HARVEST);
    }
}
