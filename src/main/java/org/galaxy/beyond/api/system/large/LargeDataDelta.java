package org.galaxy.beyond.api.system.large;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.ArrayList;
import java.util.List;

public record LargeDataDelta(
        Type type,
        ZoneType zoneType,
        List<Long> chunks,
        NodeData nodeData,
        long nodeKey,
        Identifier phaseId,
        NodeColor nodeColor
) {
    public enum Type {
        FULL_REPLACE,
        ADD_ZONE_CHUNKS,
        ADD_NODE_DATA,
        UPDATE_NODE_PHASE,
        UPDATE_NODE_COLOR,
        ADD_NODE_CHUNKS
    }

    public static LargeDataDelta addZoneChunks(ZoneType zoneType, List<Long> chunks) {
        return new LargeDataDelta(Type.ADD_ZONE_CHUNKS, zoneType, List.copyOf(chunks), null, 0L, null, null);
    }

    public static LargeDataDelta addNodeData(NodeData nodeData) {
        NodeData copy = nodeData.copy();
        return new LargeDataDelta(Type.ADD_NODE_DATA, ZoneType.Empty, List.of(), copy, copy.ensureNodeKey(), null, null);
    }

    public static LargeDataDelta updateNodePhase(long nodeKey, NodePhase phase) {
        return new LargeDataDelta(Type.UPDATE_NODE_PHASE, ZoneType.Empty, List.of(), null, nodeKey, phase.getId(), null);
    }

    public static LargeDataDelta updateNodeColor(long nodeKey, NodeColor color) {
        return new LargeDataDelta(Type.UPDATE_NODE_COLOR, ZoneType.Empty, List.of(), null, nodeKey, null, color);
    }

    public static LargeDataDelta addNodeChunks(long nodeKey, List<Long> chunks) {
        return new LargeDataDelta(Type.ADD_NODE_CHUNKS, ZoneType.Empty, List.copyOf(chunks), null, nodeKey, null, null);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        switch (type) {
            case ADD_ZONE_CHUNKS -> {
                buf.writeEnum(zoneType);
                writeLongs(buf, chunks);
            }
            case ADD_NODE_DATA -> NodeData.writeFull(buf, nodeData);
            case UPDATE_NODE_PHASE -> {
                buf.writeLong(nodeKey);
                buf.writeUtf(phaseId.toString());
            }
            case UPDATE_NODE_COLOR -> {
                buf.writeLong(nodeKey);
                NodeColor.STREAM_CODEC.encode(buf, nodeColor);
            }
            case ADD_NODE_CHUNKS -> {
                buf.writeLong(nodeKey);
                writeLongs(buf, chunks);
            }
            case FULL_REPLACE -> throw new IllegalStateException("FULL_REPLACE is encoded by BeyondLargeLevelData");
        }
    }

    public static LargeDataDelta read(FriendlyByteBuf buf) {
        Type type = buf.readEnum(Type.class);
        return switch (type) {
            case ADD_ZONE_CHUNKS -> addZoneChunks(buf.readEnum(ZoneType.class), readLongs(buf));
            case ADD_NODE_DATA -> addNodeData(NodeData.readFull(buf));
            case UPDATE_NODE_PHASE -> {
                long nodeKey = buf.readLong();
                Identifier phaseId = Identifier.parse(buf.readUtf());
                yield new LargeDataDelta(type, ZoneType.Empty, List.of(), null, nodeKey, phaseId, null);
            }
            case UPDATE_NODE_COLOR -> updateNodeColor(buf.readLong(), NodeColor.STREAM_CODEC.decode(buf));
            case ADD_NODE_CHUNKS -> addNodeChunks(buf.readLong(), readLongs(buf));
            case FULL_REPLACE -> throw new IllegalStateException("FULL_REPLACE is decoded by BeyondLargeLevelData");
        };
    }

    private static void writeLongs(FriendlyByteBuf buf, List<Long> values) {
        buf.writeVarInt(values.size());
        for (long value : values) {
            buf.writeLong(value);
        }
    }

    private static List<Long> readLongs(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Long> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(buf.readLong());
        }
        return values;
    }
}
