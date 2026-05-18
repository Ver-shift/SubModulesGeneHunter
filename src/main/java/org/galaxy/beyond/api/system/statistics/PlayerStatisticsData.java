package org.galaxy.beyond.api.system.statistics;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;

public class PlayerStatisticsData implements IPersistedSerializable {

    @Persisted
    private int loginCount;
}
