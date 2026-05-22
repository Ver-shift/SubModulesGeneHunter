package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ZoneData implements IPersistedSerializable {

    @Persisted
    private ZoneType zoneType = ZoneType.Empty;

    public static final MapCodec<ZoneData> CODEC = PersistedParser.createMapCodec(ZoneData::new);
    public static final StreamCodec<ByteBuf, ZoneData> STREAM_CODEC = PersistedParser.createStreamCodec(ZoneData::new);

    public ZoneData() {}

    public ZoneData(ZoneType zoneType) {
        this.zoneType = zoneType;
    }

    public ZoneType getZone() {
        return zoneType;
    }
}
