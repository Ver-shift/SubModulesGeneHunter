package org.galaxy.beyond.api.system.node;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;

public enum NodeColor implements IPersistedSerializable {
    BLUE(0x0000FF),
    GREEN(0x00FF00),
    ORANGE(0xFFA500),
    RED(0xFF0000),
    EMPTY(0xFFFFFF);

    @Persisted
    private final int colorValue;

    NodeColor(int colorValue) {
        this.colorValue = colorValue;
    }

    public int getColorValue() {
        return colorValue;
    }
}
