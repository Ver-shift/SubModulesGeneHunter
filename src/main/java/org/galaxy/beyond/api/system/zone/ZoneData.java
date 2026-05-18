package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import org.galaxy.beyond.api.init.BeyondZoneNodeCapInit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZoneData implements IPersistedSerializable {

    @Persisted
    private ZoneType zoneType = ZoneType.Empty;
    @Persisted(subPersisted = true)
    private final List<ZoneCapData> zoneCaps = new CopyOnWriteArrayList<>();

    public ZoneData() {}

    public ZoneData(ZoneType zoneType) {
        this.zoneType = zoneType;
        initDefaultCaps();
    }

    private void initDefaultCaps() {
        switch (zoneType) {
            case Safe_Zone -> {
                addCap(BeyondZoneNodeCapInit.ALL_SAFE_ZONE.get());
                addCap(BeyondZoneNodeCapInit.PROGRESS_START.get());
                addCap(BeyondZoneNodeCapInit.PLAYER_IN_GAME.get());
            }
            case Node_Zone -> {
                addCap(BeyondZoneNodeCapInit.ALL_NODE_ZONE.get());
                addCap(BeyondZoneNodeCapInit.NODE_CAP.get());
                addCap(BeyondZoneNodeCapInit.PLAYER_IN_GAME.get());
            }
            case Active_Zone -> {
                addCap(BeyondZoneNodeCapInit.PROGRESS_START.get());
                addCap(BeyondZoneNodeCapInit.PLAYER_IN_GAME.get());
            }
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
