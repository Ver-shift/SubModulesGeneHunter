package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.init.BeyondSceneTypes;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;

import java.util.function.Supplier;

/**
 * 资源类型遭遇（收获场景）
 */
public class HarvestEncounter extends EncounterType {
    public HarvestEncounter(Supplier<NodeColor> color) {
        super(color, BeyondSceneTypes.HARVEST);
    }
}
