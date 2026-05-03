package org.galaxy.beyond.api.system.zone;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZoneData {

    private ZoneType type;
    private List<ZoneCapData> zoneCaps = new CopyOnWriteArrayList<>();

    public void addCap(ZoneCapType cap) {
        zoneCaps.add(new ZoneCapData(cap));
    }

    public void removeCap(ZoneCapType cap) {
        zoneCaps.remove(new ZoneCapData(cap));
    }

    public void clearCaps() {
        zoneCaps.clear();
    }
}
