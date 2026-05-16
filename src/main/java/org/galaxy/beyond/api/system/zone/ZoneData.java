package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import org.galaxy.beyond.api.init.BeyondZoneNodeCapInit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZoneData implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private ZoneType zoneType = ZoneType.Empty;
    @DescSynced
    @Persisted(subPersisted = true)
    private final List<ZoneCapData> zoneCaps = new CopyOnWriteArrayList<>();

    public ZoneData() {}

    public ZoneData(ZoneType zoneType) {
        this.zoneType = zoneType;
        initDefaultCaps();
    }

    private void initDefaultCaps() {
        switch (zoneType) {
            case Safe_Zone -> addCap(BeyondZoneNodeCapInit.ALL_SAFE_ZONE.get());
            case Node_Zone -> addCap(BeyondZoneNodeCapInit.ALL_NODE_ZONE.get());
        }
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
