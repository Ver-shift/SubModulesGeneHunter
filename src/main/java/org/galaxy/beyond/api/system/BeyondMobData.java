package org.galaxy.beyond.api.system;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 给livingEntity用
 */
public class BeyondMobData implements IPersistedSerializable {

    @Persisted
    private ZoneType zoneType = ZoneType.Empty;

    public ZoneType getZoneType() { return zoneType; }
    public void setZoneType(ZoneType zoneType) { this.zoneType = zoneType; }

    public static final MapCodec<BeyondMobData> CODEC = PersistedParser.createMapCodec(BeyondMobData::new);
    public static final StreamCodec<ByteBuf, BeyondMobData> STREAM_CODEC = PersistedParser.createStreamCodec(BeyondMobData::new);
}
