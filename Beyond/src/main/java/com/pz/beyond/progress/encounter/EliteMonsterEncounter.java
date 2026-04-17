package com.pz.beyond.progress.encounter;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

/**
 * 精英怪挑战（红色 + 资源类型）
 */
public class EliteMonsterEncounter extends EncounterType {

    public static final ResourceLocation ID = Beyond.asResource("elite_monster");

    public EliteMonsterEncounter() {
        super(ID, SceneType.HARVEST, NodeColor.RED);
    }
}
