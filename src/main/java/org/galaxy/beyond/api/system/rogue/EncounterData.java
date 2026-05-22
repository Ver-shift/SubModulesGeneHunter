package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;

@Data
public class EncounterData implements IPersistedSerializable {

    @Persisted
    private EncounterType type;
    @Persisted(subPersisted = true)
    private EventTask events = new EventTask();

    public static final MapCodec<EncounterData> CODEC = PersistedParser.createMapCodec(EncounterData::new);
    public static final StreamCodec<ByteBuf, EncounterData> STREAM_CODEC = PersistedParser.createStreamCodec(EncounterData::new);
}
