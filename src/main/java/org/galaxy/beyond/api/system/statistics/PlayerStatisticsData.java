package org.galaxy.beyond.api.system.statistics;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;

public class PlayerStatisticsData implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private int loginCount;
}
