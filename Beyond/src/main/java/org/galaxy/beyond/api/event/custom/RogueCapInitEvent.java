package org.galaxy.beyond.api.event.custom;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.system.rogue.RogueData;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * RogueData 默认 Cap 初始化事件。
 * <p>
 * Beyond 和插件系统收集默认 Cap 后发布该事件，外部模组可以继续添加或移除默认 Cap。
 */
public class RogueCapInitEvent extends net.neoforged.bus.api.Event {

    private final RogueData rogueData;
    private final Set<ResourceLocation> capIds = new LinkedHashSet<>();

    public RogueCapInitEvent(RogueData rogueData, List<ResourceLocation> capIds) {
        this.rogueData = rogueData;
        this.capIds.addAll(capIds);
    }

    public RogueData getRogueData() {
        return rogueData;
    }

    public List<ResourceLocation> getCapIds() {
        return List.copyOf(capIds);
    }

    public void addCap(ResourceLocation id) {
        if (id == null) return;
        capIds.add(id);
    }

    public void removeCap(ResourceLocation id) {
        if (id == null) return;
        capIds.remove(id);
    }

    public static RogueCapInitEvent post(RogueCapInitEvent event) {
        NeoForge.EVENT_BUS.post(event);
        return event;
    }
}
