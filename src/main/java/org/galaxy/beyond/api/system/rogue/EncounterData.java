package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;

@Data
public class EncounterData implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private EncounterType type;
    @DescSynced
    @Persisted(subPersisted = true)
    private EventTask events;
}
