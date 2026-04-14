package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.progress.SceneType;

/**
 * 诅咒商店（红色 + 休息类型）
 */
public class CursedShopEncounter extends EncounterType {
    public CursedShopEncounter() {
        super(SceneType.REPOSE);
    }
}
