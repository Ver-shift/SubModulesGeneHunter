package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.definition.ProgressDefinition;
import org.galaxy.beyond.data.progress.BaseProgress;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 代码默认关卡注册容器。
 * <p>
 * 插件把默认关卡放进这里，资源包加载时再统一合并到 RogueDefinition。
 */
public class ProgressRegistration {

    private final Map<ResourceLocation, ProgressDefinition> progress = new LinkedHashMap<>();

    public void addProgress(ResourceLocation id, ProgressDefinition definition) {
        if (id == null || definition == null) return;
        progress.put(id, definition);
    }

    public void addProgress(ResourceLocation id, BaseProgress progress) {
        if (progress == null) return;
        addProgress(id, progress.build());
    }

    public void addProgress(ResourceLocation id, Supplier<ProgressDefinition> supplier) {
        if (supplier == null) return;
        addProgress(id, supplier.get());
    }

    public void addProgressBuilder(ResourceLocation id, Supplier<? extends BaseProgress> supplier) {
        if (supplier == null) return;
        addProgress(id, supplier.get());
    }

    public Map<ResourceLocation, ProgressDefinition> getProgress() {
        return progress;
    }
}
