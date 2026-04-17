package com.pz.beyond.progress.encounter;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

public class BossEncounter extends EncounterType {

    public static final ResourceLocation ID = Beyond.asResource("boss");

    public BossEncounter(NodeColor color) {
        super(ID, SceneType.CLIMAX, color);
    }
}
