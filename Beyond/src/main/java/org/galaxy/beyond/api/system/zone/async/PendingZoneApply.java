package org.galaxy.beyond.api.system.zone.async;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.List;

public class PendingZoneApply {

    private final long jobId;
    private final ZoneExpansionJobType type;
    private final List<Long> chunks;
    private final int radius;
    private final boolean success;
    private int cursor;

    public PendingZoneApply(ZoneExpansionJobType type, ZoneExpansionResult result) {
        this.jobId = result.jobId();
        this.type = type;
        this.chunks = result.addedActive();
        this.radius = result.radius();
        this.success = result.success();
    }

    public int apply(ServerLevel level, int limit) {
        if (isDone()) return 0;
        int end = Math.min(cursor + limit, chunks.size());
        List<Long> batch = chunks.subList(cursor, end);
        boolean changed = BeyondAPI.getLargeLevelData(level).addPackedZoneChunks(ZoneType.Active_Zone, batch);
        cursor = end;
        if (changed) BeyondAPI.syncLargeLevelData(level);
        return batch.size();
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

    public boolean isSuccess() {
        return success;
    }
}
