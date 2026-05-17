package org.galaxy.beyond.api;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import org.galaxy.beyond.api.system.rogue.RogueData;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureData;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

@Data
public class BeyondDimensionData implements IPersistedSerializable {

    @Persisted(subPersisted = true)
    private RogueData rogueData = new RogueData();

    @Persisted(subPersisted = true)
    private LevelZoneData levelZoneData = new LevelZoneData();

    @Persisted(subPersisted = true)
    private SafeZoneStructureData safeZoneStructureData = new SafeZoneStructureData();

    public static final MapCodec<BeyondDimensionData> CODEC = PersistedParser.createMapCodec(BeyondDimensionData::new);
    public static final Codec<BeyondDimensionData> CODEC_DIRECT = CODEC.codec();
    public static final StreamCodec<ByteBuf, BeyondDimensionData> STREAM_CODEC = PersistedParser.createStreamCodec(BeyondDimensionData::new);
}
