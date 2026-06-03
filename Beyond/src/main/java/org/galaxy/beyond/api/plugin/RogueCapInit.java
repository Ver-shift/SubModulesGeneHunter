package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * RogueData 默认 Cap 初始化容器。
 * <p>
 * 这里不注册 RogueCap 类型，只声明新 RogueData 创建时默认装载哪些已注册 Cap。
 */
public class RogueCapInit {

    private final Set<ResourceLocation> capIds = new LinkedHashSet<>();

    public void initCap(ResourceLocation id) {
        if (id == null) return;
        capIds.add(id);
    }

    public void removeCap(ResourceLocation id) {
        if (id == null) return;
        capIds.remove(id);
    }

    public List<ResourceLocation> getCapIds() {
        return List.copyOf(capIds);
    }
}
