package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Data;

@Data
public class EncounterData implements IPersistedSerializable {

    @Persisted
    private EncounterType type;
    @Persisted(subPersisted = true)
    private EventTask events;
}
