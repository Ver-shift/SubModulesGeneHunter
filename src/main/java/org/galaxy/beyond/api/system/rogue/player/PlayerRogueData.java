package org.galaxy.beyond.api.system.rogue.player;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

@Data
public class PlayerRogueData implements IPersistedSerializable {

    @DescSynced @Persisted
    private PlayerRogueState state = PlayerRogueState.LOBBY;
    @DescSynced @Persisted
    private int lifeCount;
    @DescSynced @Persisted
    private int maxLifeCount;
    @DescSynced @Persisted
    private int deathCount;
    @DescSynced @Persisted
    private long readyTimestamp;
    @DescSynced @Persisted
    private int spectatorTicks;
    @DescSynced @Persisted
    private boolean firstSpawnDone;
    @DescSynced @Persisted
    private long lastLeaveSafeZoneTime;
}
