package org.galaxy.beyond.api.system.rogue.player;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Data;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

@Data
public class PlayerRogueData implements IPersistedSerializable {

    @Persisted(subPersisted = true)
    private PlayerPhase phase = getDefault();

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

    private static PlayerPhase getDefault() {
        return BeyondPhaseInit.PLAYER_LOBBY.get();
    }
}
