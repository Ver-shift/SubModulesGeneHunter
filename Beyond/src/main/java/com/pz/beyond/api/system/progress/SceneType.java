package com.pz.beyond.api.system.progress;

import net.minecraft.resources.ResourceLocation;

/**
 * 进度类型，通过开发者手动指定，与颜色共同组成
 */
public class SceneType {
    private ResourceLocation identifier;

    public SceneType(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    /**
     * 获取Scene在进度条上面显示的条件
     * @return
     */
    public ResourceLocation getTexture(){
        return identifier;
    }

}
