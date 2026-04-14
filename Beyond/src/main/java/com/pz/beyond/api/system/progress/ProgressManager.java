package com.pz.beyond.api.system.progress;

import com.pz.beyond.api.system.progress.core.IProgressManager;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class ProgressManager implements IProgressManager {

    @Override
    public void setCurrentProgress(ResourceLocation id) {

    }

    @Override
    public void createProgress(ResourceLocation id, Consumer<ProgressType.Builder> consumer) {

    }

    @Override
    public void startGame() {

    }
}
