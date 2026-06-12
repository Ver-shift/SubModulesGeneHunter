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
 * Phase 变更事件族。
 * <p>
 * Beyond 有 Rogue、Node、Player 三类 phase。每类 phase 都会在进入、退出和 tick 时发布事件。
 * 这些事件只负责通知，不会取消默认 phase 流程；需要阻止遭遇推进时应使用 {@link RogueEncounterEvent}。
 * <p>
 * {@link #getContext()} 会延迟读取 Beyond 共享的 {@link IRogueContext}。如果管理器尚未可用，
 * 会返回一个临时 {@link RogueContext}，因此监听者在早期生命周期里应先判断自己需要的数据是否存在。
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

    /** 变更前 phase；Tick 事件中等于当前 phase。 */
    public Phase getFrom() {
        return from;
    }

    /** 变更后 phase；Tick 事件中等于当前 phase。 */
    public Phase getTo() {
        return to;
    }

    /** 当前 Rogue 上下文。 */
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

    /** phase 进入事件基类。 */
    public static abstract class Enter extends PhaseChangeEvent {
        public Enter(ServerLevel level, Phase from, Phase to) {
            super(level, from, to);
        }
    }

    /** phase 退出事件基类。 */
    public static abstract class Exit extends PhaseChangeEvent {
        public Exit(ServerLevel level, Phase from, Phase to) {
            super(level, from, to);
        }
    }

    /** phase tick 事件基类。 */
    public static abstract class Tick extends PhaseChangeEvent {
        public Tick(ServerLevel level, Phase current) {
            super(level, current, current);
        }

        @Override
        public Phase getTo() {
            return getFrom();
        }
    }

    /** Rogue 全局 phase 进入事件。 */
    public static class RogueEnter extends Enter {
        public RogueEnter(ServerLevel level, Phase from, Phase to) {
            super(level, from, to);
        }
    }

    /** Rogue 全局 phase 退出事件。 */
    public static class RogueExit extends Exit {
        public RogueExit(ServerLevel level, Phase from, Phase to) {
            super(level, from, to);
        }
    }

    /** Rogue 全局 phase tick 事件。 */
    public static class RogueTick extends Tick {
        public RogueTick(ServerLevel level, Phase current) {
            super(level, current);
        }
    }

    /** 节点 phase 进入事件。 */
    public static class NodeEnter extends Enter {
        public NodeEnter(ServerLevel level, Phase from, Phase to) {
            super(level, from, to);
        }
    }

    /** 节点 phase 退出事件。 */
    public static class NodeExit extends Exit {
        public NodeExit(ServerLevel level, Phase from, Phase to) {
            super(level, from, to);
        }
    }

    /** 节点 phase tick 事件。 */
    public static class NodeTick extends Tick {
        public NodeTick(ServerLevel level, Phase current) {
            super(level, current);
        }
    }

    /** 玩家 phase 进入事件。 */
    public static class PlayerEnter extends Enter {
        private final ServerPlayer player;

        public PlayerEnter(Phase from, Phase to, ServerPlayer player) {
            super((ServerLevel) player.level(), from, to);
            this.player = player;
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }

    /** 玩家 phase 退出事件。 */
    public static class PlayerExit extends Exit {
        private final ServerPlayer player;

        public PlayerExit(Phase from, Phase to, ServerPlayer player) {
            super((ServerLevel) player.level(), from, to);
            this.player = player;
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }

    /** 玩家 phase tick 事件。 */
    public static class PlayerTick extends Tick {
        private final ServerPlayer player;

        public PlayerTick(Phase current, ServerPlayer player) {
            super((ServerLevel) player.level(), current);
            this.player = player;
        }

        public ServerPlayer getPlayer() {
            return player;
        }
    }
}
