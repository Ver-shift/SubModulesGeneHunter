package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Data;

@Data
public class PlayerZoneData implements IPersistedSerializable {

    @Persisted
    private ZoneType currentZone = ZoneType.Empty;
    @Persisted
    private boolean safeZoneInitialized;
}
