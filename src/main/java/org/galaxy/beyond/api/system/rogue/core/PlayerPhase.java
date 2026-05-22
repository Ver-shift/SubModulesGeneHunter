package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;

/**
 * 玩家 Phase 枚举 —— 纯标记，逻辑已迁移至 RogueCap 实现类。
 */
public enum PlayerPhase implements Phase {

    LOBBY("lobby"),
    PRE_ROGUE("pre_rogue"),
    ON_PROGRESS("on_progress"),
    PRE_NODE("pre_node"),
    PRE_EVENT("pre_event"),
    ON_EVENT("on_event"),
    SPECTATOR("spectator"),
    DEAD("dead"),
    REWARD("reward"),
    PROGRESS_FINISH("progress_finish");

    private final Identifier id;

    PlayerPhase(String name) {
        this.id = Identifier.parse(Beyond.MODID + ":player/" + name);
    }

    public Identifier getId() { return id; }

    public static PlayerPhase byId(Identifier id) {
        for (var p : values())
            if (p.id.equals(id)) return p;
        return LOBBY;
    }
}
