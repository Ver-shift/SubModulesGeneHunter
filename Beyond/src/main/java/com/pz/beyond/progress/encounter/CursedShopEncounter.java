package com.pz.beyond.progress.encounter;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

/**
 * 诅咒商店（红色 + 休息类型）
 */
public class CursedShopEncounter extends EncounterType {

    public static final ResourceLocation ID = Beyond.asResource("cursed_shop");

    public CursedShopEncounter() {
        super(ID, SceneType.REPOSE, NodeColor.RED);
    }
}
