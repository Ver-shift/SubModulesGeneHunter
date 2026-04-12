package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.init.BeyondSceneTypes;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;

import java.util.function.Supplier;

public class BossEncounter extends EncounterType {
    public BossEncounter(Supplier<NodeColor> color) {
        super(color, BeyondSceneTypes.CLIMAX);
    }
}
