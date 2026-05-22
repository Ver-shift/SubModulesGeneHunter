package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.init.BeyondPhaseInit;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventTask implements IPersistedSerializable {

    @Persisted
    private List<Identifier> eventIds = new ArrayList<>();

    public static final MapCodec<EventTask> CODEC = PersistedParser.createMapCodec(EventTask::new);
    public static final StreamCodec<ByteBuf, EventTask> STREAM_CODEC = PersistedParser.createStreamCodec(EventTask::new);

    public EventTask() {}

    public EventTask(List<Identifier> eventIds) {
        this.eventIds = eventIds;
    }

    // ============================================================
    // JSON/Gson 兼容（字段名 eventIds，JSON 属性名 events）
    // ============================================================

    public List<Identifier> getEvents() {
        return eventIds;
    }

    public void setEvents(List<Identifier> events) {
        this.eventIds = events;
    }

    // ============================================================
    // 转换层 —— 外部直接用 RogueEventType 实例，内部存 Identifier
    // ============================================================

    /** 获取已解析的事件实例列表 */
    public List<RogueEventType> getEventInstances() {
        List<RogueEventType> result = new ArrayList<>();
        for (Identifier id : eventIds) {
            RogueEventType e = BeyondPhaseInit.getRogueEventType(id);
            if (e != null) result.add(e);
        }
        return result;
    }

    /** 从事件实例列表设置（自动提取 Identifier） */
    public void setEventInstances(List<RogueEventType> events) {
        this.eventIds = new ArrayList<>();
        for (RogueEventType e : events) {
            this.eventIds.add(e.getId());
        }
    }

    /** 判断是否有事件 */
    public boolean hasEvents() {
        return !eventIds.isEmpty();
    }

    /** 事件数量 */
    public int eventCount() {
        return eventIds.size();
    }
}
