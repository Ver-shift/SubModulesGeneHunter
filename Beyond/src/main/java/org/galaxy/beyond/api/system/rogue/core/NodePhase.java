package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;

/**
 * 节点 Phase 枚举 —— 纯标记，逻辑已迁移至 NodeCap。
 */
public enum NodePhase implements Phase {

    LOCKED("locked"),
    PRE_NODE("pre_node"),
    PRE_EVENT("pre_event"),
    ON_EVENT("on_event"),
    UNLOCKED("unlocked");

    private final ResourceLocation id;

    NodePhase(String name) {
        this.id = ResourceLocation.parse(Beyond.MODID + ":node/" + name);
    }

    public ResourceLocation getId() { return id; }

    public static NodePhase byId(ResourceLocation id) {
        for (var p : values())
            if (p.id.equals(id)) return p;
        return LOCKED;
    }
}
