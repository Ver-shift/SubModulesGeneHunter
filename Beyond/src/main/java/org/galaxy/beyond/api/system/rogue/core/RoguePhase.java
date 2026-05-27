package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;

/**
 * 全局 Rogue Phase 枚举 —— 纯标记，逻辑已迁移至 RogueCap 实现类。
 */
public enum RoguePhase implements Phase {

    LOBBY("lobby"),
    PRE_ROGUE("pre_rogue"),
    INIT("rogue_init"),
    ON_PROGRESS("on_progress"),
    PROGRESS_FINISH("rogue_progress_finish");

    private final ResourceLocation id;

    RoguePhase(String name) {
        this.id = ResourceLocation.parse(Beyond.MODID + ":rogue/" + name);
    }

    public ResourceLocation getId() { return id; }

    public static RoguePhase byId(ResourceLocation id) {
        for (var p : values())
            if (p.id.equals(id)) return p;
        return LOBBY;
    }
}
