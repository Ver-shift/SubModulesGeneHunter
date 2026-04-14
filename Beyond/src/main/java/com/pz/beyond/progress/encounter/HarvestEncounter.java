package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;

/**
 * 资源类型遭遇（收获场景）
 */
public class HarvestEncounter extends EncounterType {
    public HarvestEncounter(NodeColor color) {
        super(color, SceneType.HARVEST);
    }
}
