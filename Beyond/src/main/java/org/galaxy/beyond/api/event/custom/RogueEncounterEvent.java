package org.galaxy.beyond.api.event.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

import java.util.List;

/**
 * 节点遭遇生命周期事件族。
 * <p>
 * 这是外部模组介入遭遇流程的主要扩展点。核心事件执行逻辑仍由 {@link RogueEventType} 负责；
 * 本事件只在遭遇开始、单个子事件完成、整个遭遇完成时发布。
 * <p>
 * {@link Start} 和 {@link EventComplete} 可取消。取消后默认流程会停止推进，监听者需要自行决定后续行为。
 * {@link Complete} 不可取消，因为它代表遭遇已经完成，节点即将解锁。
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

    /** 当前节点遭遇类型。 */
    public EncounterType getEncounterType() {
        return encounterType;
    }

    /** 已解析出的事件任务，内部包含按顺序执行的 RogueEventType id。 */
    public EventTask getTask() {
        return task;
    }

    /** 当前 Rogue 上下文。 */
    public IRogueContext getContext() {
        return context;
    }

    @Override
    public ServerLevel getLevel() {
        return (ServerLevel) super.getLevel();
    }

    public static <T extends RogueEncounterEvent> T post(T event) {
        NeoForge.EVENT_BUS.post(event);
        return event;
    }

    /**
     * 遭遇开始事件。
     * <p>
     * 触发时机：EncounterType 已解析为 EventTask 后、第一个 RogueEventType 的 cast 调用前。
     * 取消该事件会阻止默认遭遇开始。
     */
    public static class Start extends RogueEncounterEvent implements ICancellableEvent {
        public Start(ServerLevel level, EncounterType encounterType, EventTask task, IRogueContext context) {
            super(level, encounterType, task, context);
        }

        /** 当前参与肉鸽的玩家。 */
        public List<ServerPlayer> getPlayers() {
            return getContext().playersInRogue(getLevel());
        }
    }

    /**
     * 单个子事件完成事件。
     * <p>
     * 触发时机：遭遇中的某个 RogueEventType 的 next 返回 SUCCESS 后。
     * 取消该事件会阻止事件索引推进，可用于等待额外条件或插入自定义流程。
     */
    public static class EventComplete extends RogueEncounterEvent implements ICancellableEvent {
        private final int eventIndex;
        private final int totalEvents;

        public EventComplete(ServerLevel level, EncounterType encounterType, EventTask task,
                             IRogueContext context, int eventIndex, int totalEvents) {
            super(level, encounterType, task, context);
            this.eventIndex = eventIndex;
            this.totalEvents = totalEvents;
        }

        /** 当前完成的事件索引，0-based。 */
        public int getEventIndex() {
            return eventIndex;
        }

        /** 遭遇中的事件总数。 */
        public int getTotalEvents() {
            return totalEvents;
        }
    }

    /**
     * 遭遇完成事件。
     * <p>
     * 触发时机：所有子事件都完成后、节点解锁前。该事件不可取消。
     */
    public static class Complete extends RogueEncounterEvent {
        public Complete(ServerLevel level, EncounterType encounterType, EventTask task, IRogueContext context) {
            super(level, encounterType, task, context);
        }
    }
}
