package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;

public class BossEncounter extends EncounterType {
    public BossEncounter(NodeColor color) {
        super(color, SceneType.CLIMAX);
    }
}
