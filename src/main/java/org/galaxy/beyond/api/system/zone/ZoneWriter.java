package org.galaxy.beyond.api.system.zone;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.zone.util.PackedChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZoneWriter {

    private int batchDepth;
    private boolean batchChanged;

    public boolean addZone(ServerLevel level, ChunkPos pos, ZoneType type) {
        return addZoneChunks(level, type, List.of(pos));
    }

    public boolean addZoneChunks(ServerLevel level, ZoneType type, Collection<ChunkPos> chunks) {
        boolean changed = BeyondAPI.getLargeLevelData(level).addZoneChunks(type, chunks);
        if (changed) sync(level);
        return changed;
    }

    public boolean removeZoneChunks(ServerLevel level, ZoneType type, Collection<ChunkPos> chunks) {
        return removePackedZoneChunks(level, type, packChunks(chunks));
    }

    public boolean removePackedZoneChunks(ServerLevel level, ZoneType type, Collection<Long> chunks) {
        boolean changed = BeyondAPI.getLargeLevelData(level).removePackedZoneChunks(type, chunks);
        if (changed) sync(level);
        return changed;
    }

    public boolean removeNodeData(ServerLevel level, long nodeKey) {
        boolean changed = BeyondAPI.getLargeLevelData(level).removeNodeData(nodeKey);
        if (changed) sync(level);
        return changed;
    }

    public boolean registerNodeZone(ServerLevel level, Collection<ChunkPos> chunks, NodeData nodeData) {
        var largeData = BeyondAPI.getLargeLevelData(level);
        boolean zoneChanged = largeData.addZoneChunks(ZoneType.Node_Zone, chunks);
        boolean nodeChanged = largeData.addNodeData(nodeData);
        if (zoneChanged || nodeChanged) sync(level);
        return zoneChanged || nodeChanged;
    }

    public boolean addNodeChunks(ServerLevel level, NodeData nodeData, Collection<ChunkPos> chunks) {
        var largeData = BeyondAPI.getLargeLevelData(level);
        boolean zoneChanged = largeData.addZoneChunks(ZoneType.Node_Zone, chunks);
        boolean nodeChanged = largeData.addNodeChunks(nodeData.ensureNodeKey(), packChunks(chunks));
        if (zoneChanged || nodeChanged) sync(level);
        return zoneChanged || nodeChanged;
    }

    public static List<Long> packChunks(Collection<ChunkPos> chunks) {
        List<Long> packed = new ArrayList<>(chunks.size());
        for (ChunkPos chunk : chunks) {
            packed.add(PackedChunkPos.pack(chunk));
        }
        return packed;
    }

    public void runBatch(ServerLevel level, Runnable action) {
        batchDepth++;
        try {
            action.run();
        } finally {
            batchDepth--;
            if (batchDepth == 0 && batchChanged) {
                batchChanged = false;
                syncNow(level);
            }
        }
    }

    private void sync(ServerLevel level) {
        if (batchDepth > 0) {
            batchChanged = true;
            return;
        }
        syncNow(level);
    }

    private static void syncNow(ServerLevel level) {
        BeyondAPI.syncLargeLevelData(level);
    }
}
