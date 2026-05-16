package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;

@Data
public class PlayerZoneData implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private ZoneType currentZone = ZoneType.Empty;
    @DescSynced
    @Persisted
    private boolean safeZoneInitialized;
}
