package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.Identifier;
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

    private final Identifier id;

    NodePhase(String name) {
        this.id = Identifier.parse(Beyond.MODID + ":node/" + name);
    }

    public Identifier getId() { return id; }

    public static NodePhase byId(Identifier id) {
        for (var p : values())
            if (p.id.equals(id)) return p;
        return LOCKED;
    }
}
