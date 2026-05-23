package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.Phase;

/**
 * Phase 变更事件族 —— 全局/节点/玩家三种 phase 的 enter / exit / tick。
 * <p>
 * 通过 {@link #post} 静态方法发布到 {@link NeoForge#EVENT_BUS}。
 * <p>
 * {@link IRogueContext} 由事件内部延迟加载，优先从 {@link BeyondAPI} 获取共享实例。
 */
public abstract class PhaseChangeEvent extends LevelEvent {

    protected final Phase from;
    protected final Phase to;
    private IRogueContext context;

    PhaseChangeEvent(ServerLevel level, Phase from, Phase to) {
        super(level);
        this.from = from;
        this.to = to;
    }
    public Phase getFrom() { return from; }
    public Phase getTo() { return to; }

    /** 延迟加载：优先从 BeyondManager 获取共享实例，失败则创建新 Context */
    public IRogueContext getContext() {
        if (context == null) {
            try {
                context = BeyondAPI.getBeyondManager().getRogueContext();
            } catch (Exception ignored) {
                context = new RogueContext();
            }
        }
        return context;
    }

    public static void post(PhaseChangeEvent event) {
        NeoForge.EVENT_BUS.post(event);
    }

    // ---- Enter / Exit / Tick ----

    public static abstract class Enter extends PhaseChangeEvent {
        public Enter(ServerLevel level, Phase from, Phase to) { super(level, from, to); }
    }

    public static abstract class Exit extends PhaseChangeEvent {
        public Exit(ServerLevel level, Phase from, Phase to) { super(level, from, to); }
    }

    public static abstract class Tick extends PhaseChangeEvent {
        public Tick(ServerLevel level, Phase current) { super(level, current, current); }
        @Override public Phase getTo() { return getFrom(); }
    }

    // ---- Rogue ----

    public static class RogueEnter extends Enter {
        public RogueEnter(ServerLevel level, Phase from, Phase to) { super(level, from, to); }
    }
    public static class RogueExit extends Exit {
        public RogueExit(ServerLevel level, Phase from, Phase to) { super(level, from, to); }
    }
    public static class RogueTick extends Tick {
        public RogueTick(ServerLevel level, Phase current) { super(level, current); }
    }

    // ---- Node ----

    public static class NodeEnter extends Enter {
        public NodeEnter(ServerLevel level, Phase from, Phase to) { super(level, from, to); }
    }
    public static class NodeExit extends Exit {
        public NodeExit(ServerLevel level, Phase from, Phase to) { super(level, from, to); }
    }
    public static class NodeTick extends Tick {
        public NodeTick(ServerLevel level, Phase current) { super(level, current); }
    }

    // ---- Player (level derived from player) ----

    public static class PlayerEnter extends Enter {
        private final ServerPlayer player;
        public PlayerEnter(Phase from, Phase to, ServerPlayer player) {
            super((ServerLevel) player.level(), from, to);
            this.player = player;
        }
        public ServerPlayer getPlayer() { return player; }
    }

    public static class PlayerExit extends Exit {
        private final ServerPlayer player;
        public PlayerExit(Phase from, Phase to, ServerPlayer player) {
            super((ServerLevel) player.level(), from, to);
            this.player = player;
        }
        public ServerPlayer getPlayer() { return player; }
    }

    public static class PlayerTick extends Tick {
        private final ServerPlayer player;
        public PlayerTick(Phase current, ServerPlayer player) {
            super((ServerLevel) player.level(), current);
            this.player = player;
        }
        public ServerPlayer getPlayer() { return player; }
    }
}
