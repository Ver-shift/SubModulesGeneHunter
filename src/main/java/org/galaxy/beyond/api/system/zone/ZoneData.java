package org.galaxy.beyond.api.system.zone;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZoneData {

    private final ZoneType zoneType;
    private final List<ZoneCapData> zoneCaps = new CopyOnWriteArrayList<>();

    public ZoneData(ZoneType zoneType) {
        this.zoneType = zoneType;
    }

    public ZoneType getZone() {
        return zoneType;
    }

    public void addCap(ZoneCapType cap) {
        zoneCaps.add(new ZoneCapData(cap));
    }

    public void removeCap(ZoneCapType cap) {
        ZoneCapData target = getCapData(cap);
        if (target != null) {
            zoneCaps.remove(target);
        }
    }

    public void clearCaps() {
        zoneCaps.clear();
    }

    public List<ZoneCapData> getZoneCaps() {
        return zoneCaps;
    }

    public ZoneCapData getCapData(ZoneCapType cap) {
        for (ZoneCapData data : zoneCaps) {
            if (data.getType().equals(cap)) {
                return data;
            }
        }
        return null;
    }
}
