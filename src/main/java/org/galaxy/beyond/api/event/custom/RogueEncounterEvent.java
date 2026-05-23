package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.IRogueContext;

import java.util.List;

/**
 * 节点遭遇 NeoForge 事件 —— 在遭遇生命周期关键节点发布到 {@link NeoForge#EVENT_BUS}，供外部模组/脚本挂载附加逻辑。
 * <p>
 * <b>角色定位：</b>这是<u>扩展点</u>，不是核心执行逻辑。核心事件执行请看 {@link RogueEventType}。
 * <p>
 * 触发时机：
 * <ul>
 *   <li>{@link Start} — 遭遇解析完成，第一个事件 cast 之前</li>
 *   <li>{@link EventComplete} — 遭遇中某个子事件 next() 返回 SUCCESS 后</li>
 *   <li>{@link Complete} — 所有事件完成，节点即将解锁（不可取消）</li>
 * </ul>
 * <p>
 * <b>Start / EventComplete 实现 {@link ICancellableEvent}，可被取消以阻止默认行为。</b><br>
 * <b>Complete 不可取消，节点解锁是强制的。</b>
 */
public abstract class RogueEncounterEvent extends LevelEvent {

    protected final EncounterType encounterType;
    protected final EventTask task;
    protected final IRogueContext context;

    RogueEncounterEvent(ServerLevel level, EncounterType encounterType, EventTask task, IRogueContext context) {
        super(level);
        this.encounterType = encounterType;
        this.task = task;
        this.context = context;
    }

    public EncounterType getEncounterType() { return encounterType; }
    public EventTask getTask() { return task; }
    public IRogueContext getContext() { return context; }

    @Override public ServerLevel getLevel() { return (ServerLevel) super.getLevel(); }

    public static <T extends RogueEncounterEvent> T post(T event) {
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    // ---- 遭遇开始 ----

    /** 遭遇解析完成，事件链即将开始 */
    public static class Start extends RogueEncounterEvent implements ICancellableEvent {
        public Start(ServerLevel level, EncounterType encounterType, EventTask task,
                     IRogueContext context) {
            super(level, encounterType, task, context);
        }

        /** 遭遇参与者 */
        public List<ServerPlayer> getPlayers() { return getContext().playersInRogue(getLevel()); }
    }

    // ---- 单个事件完成 ----

    /** 遭遇中某个子事件完成 */
    public static class EventComplete extends RogueEncounterEvent implements ICancellableEvent {
        private final int eventIndex;
        private final int totalEvents;

        public EventComplete(ServerLevel level, EncounterType encounterType, EventTask task,
                             IRogueContext context, int eventIndex, int totalEvents) {
            super(level, encounterType, task, context);
            this.eventIndex = eventIndex;
            this.totalEvents = totalEvents;
        }

        /** 当前完成的事件索引（0-based） */
        public int getEventIndex() { return eventIndex; }
        /** 遭遇中的事件总数 */
        public int getTotalEvents() { return totalEvents; }
    }

    // ---- 遭遇结束（节点解锁） ----

    /** 遭遇中所有事件完成，节点解锁 */
    public static class Complete extends RogueEncounterEvent {
        public Complete(ServerLevel level, EncounterType encounterType, EventTask task,
                        IRogueContext context) {
            super(level, encounterType, task, context);
        }
    }
}
