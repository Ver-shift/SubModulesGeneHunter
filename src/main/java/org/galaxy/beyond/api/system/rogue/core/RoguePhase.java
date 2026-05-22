package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.Identifier;
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

    private final Identifier id;

    RoguePhase(String name) {
        this.id = Identifier.parse(Beyond.MODID + ":rogue/" + name);
    }

    public Identifier getId() { return id; }

    public static RoguePhase byId(Identifier id) {
        for (var p : values())
            if (p.id.equals(id)) return p;
        return LOBBY;
    }
}
