package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;

/**
 * 休息类型遭遇（休整场景）
 */
public class ReposeEncounter extends EncounterType {
    public ReposeEncounter(NodeColor color) {
        super(color, SceneType.REPOSE);
    }
}
