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
    private final int reachedNodeZones;
    private final boolean success;
    private int cursor;

    public PendingZoneApply(ZoneExpansionJobType type, ZoneExpansionResult result) {
        this.jobId = result.jobId();
        this.type = type;
        this.chunks = result.addedActive();
        this.radius = result.radius();
        this.reachedNodeZones = result.reachedNodeZones();
        this.success = result.success();
    }

    public int apply(ServerLevel level, int limit) {
        if (isDone()) return 0;
        int end = Math.min(cursor + limit, chunks.size());
        List<Long> batch = chunks.subList(cursor, end);
        List<Long> available = filterAvailable(level, batch);
        boolean changed = BeyondAPI.getLargeLevelData(level).addPackedZoneChunks(ZoneType.Active_Zone, available);
        cursor = end;
        if (changed) BeyondAPI.syncLargeLevelData(level);
        org.galaxy.beyond.Beyond.debugInfo(
                "[Zone][EXPAND_APPLY] jobId={}, type={}, batch={}, available={}, cursor={}/{}, changed={}",
                jobId,
                type,
                batch.size(),
                available.size(),
                cursor,
                chunks.size(),
                changed
        );
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
        return reachedNodeZones;
    }

    public boolean isSuccess() {
        return success;
    }
}
