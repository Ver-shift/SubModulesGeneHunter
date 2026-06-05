package org.galaxy.beyond.api.system.zone.async;

import java.util.List;
import java.util.Set;

public record ZoneExpansionRequest(
        long jobId,
        ZoneExpansionJobType type,
        Set<Long> seeds,
        Set<Long> safe,
        Set<Long> node,
        Set<Long> active,
        Set<Long> uncompleted,
        List<Set<Long>> targetNodeAreas,
        int minConnections,
        int minRadius,
        int maxRadius
) {
}
