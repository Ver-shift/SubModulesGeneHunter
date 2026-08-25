package org.galaxy.beyond.api.system.large;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BeyondLargeLevelData implements IPersistedSerializable {

    public static final MapCodec<BeyondLargeLevelData> CODEC = PersistedParser.createMapCodec(BeyondLargeLevelData::new);
    public static final Codec<BeyondLargeLevelData> CODEC_DIRECT = CODEC.codec();
    public static final StreamCodec<ByteBuf, BeyondLargeLevelData> STREAM_CODEC = PersistedParser.createStreamCodec(BeyondLargeLevelData::new);

    @Getter
    @Persisted(subPersisted = true)
    private LevelZoneData levelZoneData = new LevelZoneData();

    @Getter
    @Persisted
    private final List<NodeData> nodeDatas = new CopyOnWriteArrayList<>();

    private transient final List<LargeDataDelta> pendingDeltas = new ArrayList<>();
    private transient List<LargeDataDelta> syncSnapshot = List.of();
    /** Set while a full attachment snapshot is being sent to newly connected clients. */
    private transient boolean fullSyncRequested;

    public boolean addZoneChunks(ZoneType type, Collection<ChunkPos> chunks) {
        List<Long> added = new ArrayList<>();
        for (ChunkPos chunk : chunks) {
            long packed = pack(chunk);
            if (levelZoneData.addPacked(type, packed)) {
                added.add(packed);
            }
        }
        if (added.isEmpty()) return false;
        recordDelta(LargeDataDelta.addZoneChunks(type, added));
        return true;
    }

    public boolean recordAddedZoneChunks(ZoneType type, Collection<ChunkPos> chunks) {
        if (chunks.isEmpty()) return false;
        List<Long> packedChunks = new ArrayList<>(chunks.size());
        for (ChunkPos chunk : chunks) {
            packedChunks.add(pack(chunk));
        }
        recordDelta(LargeDataDelta.addZoneChunks(type, packedChunks));
        return true;
    }

    public boolean addPackedZoneChunks(ZoneType type, Collection<Long> chunks) {
        List<Long> added = new ArrayList<>();
        for (long chunk : chunks) {
            if (levelZoneData.addPacked(type, chunk)) {
                added.add(chunk);
            }
        }
        if (added.isEmpty()) return false;
        recordDelta(LargeDataDelta.addZoneChunks(type, added));
        return true;
    }

    public boolean removePackedZoneChunks(ZoneType type, Collection<Long> chunks) {
        List<Long> removed = new ArrayList<>();
        for (long chunk : chunks) {
            if (levelZoneData.removePacked(type, chunk)) {
                removed.add(chunk);
            }
        }
        if (removed.isEmpty()) return false;
        recordDelta(LargeDataDelta.removeZoneChunks(type, removed));
        return true;
    }

    public boolean addNodeData(NodeData nodeData) {
        nodeData.ensureNodeKey();
        if (nodeData.getNodeKey() == 0L || findNodeDataByKey(nodeData.getNodeKey()) != null) {
            return false;
        }
        NodeData copy = nodeData.copy();
        nodeDatas.add(copy);
        recordDelta(LargeDataDelta.addNodeData(copy));
        return true;
    }

    public boolean removeNodeData(long nodeKey) {
        if (nodeDatas.removeIf(nodeData -> nodeData.ensureNodeKey() == nodeKey)) {
            recordDelta(LargeDataDelta.removeNodeData(nodeKey));
            return true;
        }
        return false;
    }

    public boolean addNodeChunks(long nodeKey, Collection<Long> chunks) {
        NodeData node = findNodeDataByKey(nodeKey);
        if (node == null) return false;
        List<Long> added = new ArrayList<>();
        for (long chunk : chunks) {
            if (node.addPackedChunkIfAbsent(chunk)) {
                added.add(chunk);
            }
        }
        if (added.isEmpty()) return false;
        recordDelta(LargeDataDelta.addNodeChunks(nodeKey, added));
        return true;
    }

    public boolean updateNodePhase(long nodeKey, NodePhase phase) {
        NodeData node = findNodeDataByKey(nodeKey);
        if (node == null || node.getPhase() == phase) return false;
        node.setPhase(phase);
        recordDelta(LargeDataDelta.updateNodePhase(nodeKey, phase));
        return true;
    }

    public boolean updateNodePhase(NodeData nodeData, NodePhase phase) {
        return updateNodePhase(nodeData.ensureNodeKey(), phase);
    }

    public boolean updateNodePhaseById(long nodeKey, ResourceLocation phaseId) {
        return updateNodePhase(nodeKey, NodePhase.byId(phaseId));
    }

    public boolean updateNodeColor(long nodeKey, NodeColor color) {
        NodeData node = findNodeDataByKey(nodeKey);
        if (node == null || node.getColor() == color) return false;
        node.setColor(color);
        recordDelta(LargeDataDelta.updateNodeColor(nodeKey, color));
        return true;
    }

    public boolean updateNodeColor(NodeData nodeData, NodeColor color) {
        return updateNodeColor(nodeData.ensureNodeKey(), color);
    }

    public NodeData findNodeData(ChunkPos pos) {
        for (NodeData nodeData : nodeDatas) {
            if (nodeData.containsChunk(pos)) return nodeData;
        }
        return null;
    }

    public NodeData findNodeDataByKey(long nodeKey) {
        for (NodeData nodeData : nodeDatas) {
            if (nodeData.ensureNodeKey() == nodeKey) return nodeData;
        }
        return null;
    }

    public boolean isEmpty() {
        return !levelZoneData.hasZones() && nodeDatas.isEmpty();
    }

    public void copyFrom(LevelZoneData zones, List<NodeData> nodes) {
        levelZoneData.replaceFrom(zones);
        nodeDatas.clear();
        for (NodeData nodeData : nodes) {
            nodeDatas.add(nodeData.copy());
        }
        clearSyncState();
    }

    public List<LargeDataDelta> drainPendingDeltasForSync() {
        List<LargeDataDelta> drained = List.copyOf(pendingDeltas);
        pendingDeltas.clear();
        return drained;
    }

    public void prepareSyncSnapshot() {
        syncSnapshot = drainPendingDeltasForSync();
        fullSyncRequested = false;
    }

    public void prepareFullSyncSnapshot() {
        // The full payload supersedes any queued deltas; do not replay them on the
        // next incremental sync after the client has received this snapshot.
        syncSnapshot = drainPendingDeltasForSync();
        fullSyncRequested = true;
    }

    public boolean isFullSyncRequested() {
        return fullSyncRequested;
    }

    public void clearFullSyncRequest() {
        fullSyncRequested = false;
    }

    public List<LargeDataDelta> getSyncSnapshot() {
        return syncSnapshot;
    }

    public int pendingDeltaCount() {
        return pendingDeltas.size();
    }

    public void applyDelta(LargeDataDelta delta) {
        switch (delta.type()) {
            case ADD_ZONE_CHUNKS -> levelZoneData.addPackedAll(delta.zoneType(), delta.chunks());
            case REMOVE_ZONE_CHUNKS -> levelZoneData.removePackedAll(delta.zoneType(), delta.chunks());
            case ADD_NODE_DATA -> {
                NodeData node = delta.nodeData().copy();
                if (findNodeDataByKey(node.ensureNodeKey()) == null) {
                    nodeDatas.add(node);
                }
            }
            case REMOVE_NODE_DATA -> removeNodeDataNoDelta(delta.nodeKey());
            case UPDATE_NODE_PHASE -> updateNodePhaseByIdNoDelta(delta.nodeKey(), delta.phaseId());
            case UPDATE_NODE_COLOR -> updateNodeColorNoDelta(delta.nodeKey(), delta.nodeColor());
            case ADD_NODE_CHUNKS -> addNodeChunksNoDelta(delta.nodeKey(), delta.chunks());
            case FULL_REPLACE -> throw new IllegalStateException("FULL_REPLACE is applied by full data decoding");
        }
    }

    public void writeFull(FriendlyByteBuf buf) {
        LevelZoneData.writeFull(buf, levelZoneData);
        buf.writeVarInt(nodeDatas.size());
        for (NodeData nodeData : nodeDatas) {
            NodeData.writeFull(buf, nodeData);
        }
    }

    public static BeyondLargeLevelData readFull(FriendlyByteBuf buf) {
        BeyondLargeLevelData data = new BeyondLargeLevelData();
        data.levelZoneData = LevelZoneData.readFull(buf);
        int nodes = buf.readVarInt();
        for (int i = 0; i < nodes; i++) {
            data.nodeDatas.add(NodeData.readFull(buf));
        }
        return data;
    }

    private boolean updateNodePhaseByIdNoDelta(long nodeKey, ResourceLocation phaseId) {
        NodeData node = findNodeDataByKey(nodeKey);
        if (node == null) return false;
        node.setPhase(NodePhase.byId(phaseId));
        return true;
    }

    private boolean updateNodeColorNoDelta(long nodeKey, NodeColor color) {
        NodeData node = findNodeDataByKey(nodeKey);
        if (node == null) return false;
        node.setColor(color);
        return true;
    }

    private boolean addNodeChunksNoDelta(long nodeKey, List<Long> chunks) {
        NodeData node = findNodeDataByKey(nodeKey);
        if (node == null) return false;
        boolean changed = false;
        for (long chunk : chunks) {
            if (node.addPackedChunkIfAbsent(chunk)) changed = true;
        }
        return changed;
    }

    private boolean removeNodeDataNoDelta(long nodeKey) {
        return nodeDatas.removeIf(nodeData -> nodeData.ensureNodeKey() == nodeKey);
    }

    private void recordDelta(LargeDataDelta delta) {
        pendingDeltas.add(delta);
    }

    private void clearSyncState() {
        pendingDeltas.clear();
        syncSnapshot = List.of();
    }

    private static long pack(ChunkPos pos) {
        return ((long) pos.x & 0xFFFFFFFFL) | (((long) pos.z & 0xFFFFFFFFL) << 32);
    }

}
