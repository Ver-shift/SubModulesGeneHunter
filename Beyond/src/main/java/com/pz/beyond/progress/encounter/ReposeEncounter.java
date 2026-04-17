package com.pz.beyond.progress.encounter;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

/**
 * 休息类型遭遇（休整场景）
 */
public class ReposeEncounter extends EncounterType {

    public static final ResourceLocation ID = Beyond.asResource("repose");

    public ReposeEncounter(NodeColor color) {
        super(ID, SceneType.REPOSE, color);
    }
}
