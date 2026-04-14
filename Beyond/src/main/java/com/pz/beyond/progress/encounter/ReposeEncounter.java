package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.init.BeyondSceneTypes;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;

import java.util.function.Supplier;

/**
 * 休息类型遭遇（休整场景）
 */
public class ReposeEncounter extends EncounterType {
    public ReposeEncounter(Supplier<NodeColor> color) {
        super(color, BeyondSceneTypes.REPOSE);
    }
}
