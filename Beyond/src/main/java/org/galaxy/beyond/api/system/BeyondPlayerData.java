package org.galaxy.beyond.api.system;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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

    @Persisted(subPersisted = true)
    private PlayerRogueData playerRogueData = new PlayerRogueData();
    @Persisted(subPersisted = true)
    private PlayerZoneData playerZoneData = new PlayerZoneData();
    @Persisted(subPersisted = true)
    private PlayerStatisticsData playerStatisticsData = new PlayerStatisticsData();

    public static final MapCodec<BeyondPlayerData> CODEC = PersistedParser.createMapCodec(BeyondPlayerData::new);
    public static final StreamCodec<ByteBuf, BeyondPlayerData> STREAM_CODEC = PersistedParser.createStreamCodec(BeyondPlayerData::new);
}
