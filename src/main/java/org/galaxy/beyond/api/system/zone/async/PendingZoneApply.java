package org.galaxy.beyond.api.system.zone.async;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PendingZoneApply {

    private final long jobId;
    private final ZoneExpansionJobType type;
    private final List<Long> chunks;
    private final int radius;
    private final int discoveredNodeZones;
    private final int newNodeZones;
    private final int targetNodeZones;
    private final int neededNodeZones;
    private final int nearestTargetDistance;
    private final int furthestReachedDistance;
    private final boolean success;
    private int cursor;

    public PendingZoneApply(ZoneExpansionJobType type, ZoneExpansionResult result) {
        this.jobId = result.jobId();
        this.type = type;
        this.chunks = result.addedActive();
        this.radius = result.radius();
        this.discoveredNodeZones = result.discoveredNodeZones();
        this.newNodeZones = result.newNodeZones();
        this.targetNodeZones = result.targetNodeZones();
        this.neededNodeZones = result.neededNodeZones();
        this.nearestTargetDistance = result.nearestTargetDistance();
        this.furthestReachedDistance = result.furthestReachedDistance();
        this.success = result.success();
    }

    public int apply(ServerLevel level, int limit) {
        if (isDone()) return 0;
        long totalStart = ZoneProfiler.nanoTime();
        int end = Math.min(cursor + limit, chunks.size());
        List<Long> batch = chunks.subList(cursor, end);
        long stepStart = ZoneProfiler.nanoTime();
        List<Long> available = filterAvailable(level, batch);
        double filterMillis = ZoneProfiler.elapsedMillis(stepStart);
        stepStart = ZoneProfiler.nanoTime();
        boolean changed = BeyondAPI.getLargeLevelData(level).addPackedZoneChunks(ZoneType.Active_Zone, available);
        double writeMillis = ZoneProfiler.elapsedMillis(stepStart);
        cursor = end;
        stepStart = ZoneProfiler.nanoTime();
        if (changed) BeyondAPI.syncLargeLevelData(level);
        double syncMillis = ZoneProfiler.elapsedMillis(stepStart);
        ZoneProfiler.logApply(jobId, type, end - batch.size(), end, batch.size(), available.size(),
                filterMillis, writeMillis, syncMillis, ZoneProfiler.elapsedMillis(totalStart));
        return batch.size();
    }

    private static List<Long> filterAvailable(ServerLevel level, List<Long> batch) {
        var data = BeyondAPI.getLevelZoneData(level);
        Set<Long> blocked = new HashSet<>(data.getPacked(ZoneType.Safe_Zone));
        blocked.addAll(data.getPacked(ZoneType.Node_Zone));
        List<Long> filtered = new ArrayList<>(batch.size());
        for (long chunk : batch) {
            if (blocked.contains(chunk)) continue;
            filtered.add(chunk);
        }
        return filtered;
    }

    public boolean isDone() {
        return cursor >= chunks.size();
    }

    public int getCursor() {
        return cursor;
    }

    public int getTotal() {
        return chunks.size();
    }

    public long getJobId() {
        return jobId;
    }

    public ZoneExpansionJobType getType() {
        return type;
    }

    public int getRadius() {
        return radius;
    }

    public int getReachedNodeZones() {
        return discoveredNodeZones + newNodeZones;
    }

    public int getDiscoveredNodeZones() {
        return discoveredNodeZones;
    }

    public int getNewNodeZones() {
        return newNodeZones;
    }

    public int getTargetNodeZones() {
        return targetNodeZones;
    }

    public int getNeededNodeZones() {
        return neededNodeZones;
    }

    public int getNearestTargetDistance() {
        return nearestTargetDistance;
    }

    public int getFurthestReachedDistance() {
        return furthestReachedDistance;
    }

    public boolean isSuccess() {
        return success;
    }
}
