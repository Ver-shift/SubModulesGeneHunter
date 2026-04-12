package com.pz.beyond.progress.scene;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.resources.ResourceLocation;

public class BossScene extends SceneType {
    public static final ResourceLocation REPOSE_SCENE = Beyond.asResource("harvest");

    public BossScene() {
        super(REPOSE_SCENE);

    }
}
