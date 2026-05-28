package org.galaxy.beyond.api.system.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.util.CompatUtil;

import java.util.Set;

public class SafeZoneRegistrar {

    private final ZoneWriter writer;
    private final ZoneConflictResolver conflictResolver;

    public SafeZoneRegistrar(ZoneWriter writer, ZoneConflictResolver conflictResolver) {
        this.writer = writer;
        this.conflictResolver = conflictResolver;
    }

    public void addSafeZone(ServerLevel level, int chunkSize, BlockPos center) {
        ChunkPos centerChunk = CompatUtil.chunkPos(center);
        int radius = chunkSize / 2;
        Set<ChunkPos> targets = ZoneHelper.expandSquare(centerChunk, radius);

        if (!conflictResolver.clearLockedNodeConflicts(level, targets)) return;

        writer.addZoneChunks(level, ZoneType.Safe_Zone, targets);
    }
}
