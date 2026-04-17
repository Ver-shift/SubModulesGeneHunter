package com.pz.beyond.progress.encounter;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

/**
 * 篝火，纯粹回复血量（绿色 + 休息类型）
 */
public class BonfireEncounter extends EncounterType {

    public static final ResourceLocation ID = Beyond.asResource("bonfire");

    public BonfireEncounter() {
        super(ID, SceneType.REPOSE, NodeColor.GREEN);
    }
}
