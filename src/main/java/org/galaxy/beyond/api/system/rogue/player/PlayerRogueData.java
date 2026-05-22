package org.galaxy.beyond.api.system.rogue.player;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

@Data
public class PlayerRogueData implements IPersistedSerializable {

    @Persisted
    private Identifier phaseId = PlayerPhase.LOBBY.getId();

    @Persisted
    private int lifeCount;
    @Persisted
    private int maxLifeCount;
    @Persisted
    private int deathCount;
    @Persisted
    private long readyTimestamp;
    @Persisted
    private int spectatorTicks;
    @Persisted
    private boolean firstSpawnDone;
    /** 上次回到安全区的时间，用于离开冷却 */
    @Persisted
    private long lastSafeZoneReturnTime;
    /** 上次进入节点区的时间，用于消息防刷 */
    @Persisted
    private long lastNodeEnterTime;

    public static final MapCodec<PlayerRogueData> CODEC = PersistedParser.createMapCodec(PlayerRogueData::new);
    public static final StreamCodec<ByteBuf, PlayerRogueData> STREAM_CODEC = PersistedParser.createStreamCodec(PlayerRogueData::new);

    public PlayerPhase getPhase() {
        return PlayerPhase.byId(phaseId);
    }

    public void setPhase(PlayerPhase phase) {
        this.phaseId = phase.getId();
    }
}
