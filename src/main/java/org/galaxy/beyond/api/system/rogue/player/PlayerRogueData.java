package org.galaxy.beyond.api.system.rogue.player;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

@Data
public class PlayerRogueData implements IPersistedSerializable {

    @Persisted
    private PlayerRogueState state = PlayerRogueState.LOBBY;
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
    @Persisted
    private long lastLeaveSafeZoneTime;
}
