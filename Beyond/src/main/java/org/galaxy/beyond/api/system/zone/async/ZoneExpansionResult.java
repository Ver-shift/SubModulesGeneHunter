package org.galaxy.beyond.api.system.zone.async;

import java.util.List;

public record ZoneExpansionResult(
        long jobId,
        List<Long> addedActive,
        int radius,
        int discoveredNodeZones,
        int newNodeZones,
        boolean success
) {
    public int reachedNodeZones() {
        return discoveredNodeZones + newNodeZones;
    }
}
