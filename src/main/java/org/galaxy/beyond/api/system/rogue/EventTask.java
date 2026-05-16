package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.init.BeyondRogueEventTypeInit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Data
public class EventTask implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private List<Identifier> eventIds = new ArrayList<>();

    public static final MapCodec<EventTask> CODEC = PersistedParser.createMapCodec(EventTask::new);
    public static final StreamCodec<ByteBuf, EventTask> STREAM_CODEC = PersistedParser.createStreamCodec(EventTask::new);

    public EventTask() {}

    public EventTask(List<RogueEventType> events) {
        setEvents(events);
    }

    public EventTask(Supplier<RogueEventType>... rogueEventTypes) {
        for (Supplier<RogueEventType> supplier : rogueEventTypes) {
            this.eventIds.add(supplier.get().getId());
        }
    }

    public List<RogueEventType> getEvents() {
        return eventIds.stream()
                .map(id -> BeyondRogueEventTypeInit.getById(id)
                        .map(ref -> ref.value())
                        .orElse(null))
                .filter(e -> e != null)
                .collect(java.util.stream.Collectors.toList());
    }

    public void setEvents(List<RogueEventType> events) {
        this.eventIds = events.stream().map(RogueEventType::getId).collect(java.util.stream.Collectors.toList());
    }
}
