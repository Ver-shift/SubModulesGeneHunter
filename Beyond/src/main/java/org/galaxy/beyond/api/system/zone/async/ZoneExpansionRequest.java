package org.galaxy.beyond.api.system.zone.async;

import java.util.Set;

public record ZoneExpansionRequest(
        long jobId,
        Set<Long> seeds,
        Set<Long> safe,
        Set<Long> node,
        Set<Long> active,
        Set<Long> uncompleted,
        int minConnections,
        int minRadius,
        int maxRadius
) {
}
