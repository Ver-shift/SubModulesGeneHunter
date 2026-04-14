package com.pz.beyond.api.system.progress.core;

import com.pz.beyond.api.system.progress.ProgressType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public interface IProgressManager {

    /**
     * 选择当前关卡
     * @param id
     */
    void setCurrentProgress(ResourceLocation id);

    /**
     * 创建一个关卡
     * @param id
     * @param consumer
     */
    void createProgress(ResourceLocation id, Consumer<ProgressType.Builder> consumer);

    /**
     * 开启一把游戏
     *
     */
    void startGame();
}
