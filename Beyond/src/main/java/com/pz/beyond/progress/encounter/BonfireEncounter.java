package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.init.BeyondSceneTypes;
import com.pz.beyond.api.system.node.EncounterType;

/**
 * 篝火，纯粹回复血量（绿色 + 休息类型）
 */
public class BonfireEncounter extends EncounterType {
    public BonfireEncounter() {
        super(BeyondSceneTypes.REPOSE);
    }
}
