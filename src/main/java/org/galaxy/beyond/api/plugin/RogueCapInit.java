package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * RogueData 默认 Cap 初始化容器。
 * <p>
 * 这里只声明新 RogueData 默认装载哪些已注册 Cap，不负责注册 Cap 类型。
 */
public class RogueCapInit {

    private final Set<Identifier> capIds = new LinkedHashSet<>();

    public void initCap(Identifier id) {
        if (id == null) return;
        capIds.add(id);
    }

    public void removeCap(Identifier id) {
        if (id == null) return;
        capIds.remove(id);
    }

    public List<Identifier> getCapIds() {
        return List.copyOf(capIds);
    }
}
