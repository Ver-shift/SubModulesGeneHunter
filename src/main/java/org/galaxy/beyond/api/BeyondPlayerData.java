package org.galaxy.beyond.api;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueData;
import org.galaxy.beyond.api.system.statistics.PlayerStatisticsData;
import org.galaxy.beyond.api.system.zone.PlayerZoneData;

@Data
public class BeyondPlayerData implements IPersistedSerializable {

    @DescSynced
    @Persisted(subPersisted = true)
    private final PlayerRogueData playerRogueData = new PlayerRogueData();
    @DescSynced
    @Persisted(subPersisted = true)
    private final PlayerZoneData playerZoneData = new PlayerZoneData();
    @DescSynced
    @Persisted(subPersisted = true)
    private final PlayerStatisticsData playerStatisticsData = new PlayerStatisticsData();

    public static final MapCodec<BeyondPlayerData> CODEC = PersistedParser.createMapCodec(BeyondPlayerData::new);
    public static final StreamCodec<ByteBuf, BeyondPlayerData> STREAM_CODEC = PersistedParser.createStreamCodec(BeyondPlayerData::new);
}
