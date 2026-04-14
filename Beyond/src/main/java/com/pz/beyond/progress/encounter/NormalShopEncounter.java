package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.init.BeyondSceneTypes;
import com.pz.beyond.api.system.node.EncounterType;

/**
 * 普通商店（橙色 + 休息类型）
 */
public class NormalShopEncounter extends EncounterType {
    public NormalShopEncounter() {
        super(BeyondSceneTypes.REPOSE);
    }
}
