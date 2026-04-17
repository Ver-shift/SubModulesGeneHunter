package com.pz.beyond.progress.encounter;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

/**
 * 有趣的事件或解密（绿色 + 资源类型）
 */
public class PuzzleEncounter extends EncounterType {

    public static final ResourceLocation ID = Beyond.asResource("puzzle");

    public PuzzleEncounter() {
        super(ID, SceneType.HARVEST, NodeColor.GREEN);
    }
}
