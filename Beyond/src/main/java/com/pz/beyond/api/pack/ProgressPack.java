package com.pz.beyond.api.pack;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class ProgressPack extends SimplePreparableReloadListener {
    @Override
    protected Object prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Object object, ResourceManager resourceManager, ProfilerFiller profiler) {

    }
}
