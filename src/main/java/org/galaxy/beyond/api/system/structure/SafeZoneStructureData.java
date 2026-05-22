package org.galaxy.beyond.api.system.structure;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

@Data
public class SafeZoneStructureData implements IPersistedSerializable {

    @Persisted
    private int initialized;
    @Persisted
    private BlockPos spawnPos = BlockPos.ZERO;
    @Persisted
    private BlockPos centerPos = BlockPos.ZERO;

    public static final MapCodec<SafeZoneStructureData> CODEC = PersistedParser.createMapCodec(SafeZoneStructureData::new);
    public static final StreamCodec<ByteBuf, SafeZoneStructureData> STREAM_CODEC = PersistedParser.createStreamCodec(SafeZoneStructureData::new);
}
