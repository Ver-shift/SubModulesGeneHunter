package com.pz.beyond.progress.encounter;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

/**
 * 普通商店（橙色 + 休息类型）
 */
public class NormalShopEncounter extends EncounterType {

    public static final ResourceLocation ID = Beyond.asResource("normal_shop");

    public NormalShopEncounter() {
        super(ID, SceneType.REPOSE, NodeColor.ORANGE);
    }
}
