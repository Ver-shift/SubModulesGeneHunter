package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.progress.SceneType;

/**
 * 普通商店（橙色 + 休息类型）
 */
public class NormalShopEncounter extends EncounterType {
    public NormalShopEncounter() {
        super(SceneType.REPOSE);
    }
}
