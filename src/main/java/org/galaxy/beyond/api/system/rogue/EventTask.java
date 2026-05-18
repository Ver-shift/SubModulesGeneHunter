package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

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

    public List<Identifier> getEvents() {
        return eventIds;
    }

    public void setEvents(List<Identifier> events) {
        this.eventIds = events;
    }
}
