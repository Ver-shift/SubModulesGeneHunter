package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 节点的静态数据，NodeZone 加载时创建，存储在 ProgressCatalog 中。
 * 只包含节点生成时就固定的信息。
 * chunkKey 为 ChunkPos.toLong()，与 Zone 系统保持一致。
 */
public class NodeData {

    public static final String NODE_COLOR = "node_color";
    public static final String CHUNK_KEY = "chunk_key";
    public static final String NODE_STATE = "node_state";

    public static final Codec<NodeData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        NodeColor.CODEC.fieldOf(NODE_COLOR).forGetter(NodeData::getNodeColor),
        Codec.LONG.fieldOf(CHUNK_KEY).forGetter(NodeData::getChunkKey),
        NodeState.CODEC.optionalFieldOf(NODE_STATE, NodeState.LOCKED).forGetter(NodeData::getNodeState)
    ).apply(builder, NodeData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Long> LONG_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Long decode(RegistryFriendlyByteBuf buf) {
            return buf.readLong();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Long value) {
            buf.writeLong(value);
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeData> STREAM_CODEC = StreamCodec.composite(
        NodeColor.STREAM_CODEC,
        NodeData::getNodeColor,
        LONG_STREAM_CODEC,
        NodeData::getChunkKey,
        NodeState.STREAM_CODEC,
        NodeData::getNodeState,
        NodeData::new
    );

    /**
     * 节点颜色，生成时固定
     */
    private final NodeColor nodeColor;

    /**
     * 节点所在区块的 key，ChunkPos.toLong()
     */
    private final long chunkKey;

    /**
     * 节点状态
     */
    private NodeState nodeState = NodeState.LOCKED;

    public NodeData(NodeColor nodeColor, long chunkKey) {
        this.nodeColor = nodeColor;
        this.chunkKey = chunkKey;
    }

    public NodeData(NodeColor nodeColor, long chunkKey, NodeState nodeState) {
        this.nodeColor = nodeColor;
        this.chunkKey = chunkKey;
        this.nodeState = nodeState == null ? NodeState.LOCKED : nodeState;
    }

    public NodeColor getNodeColor() {
        return nodeColor;
    }

    public long getChunkKey() {
        return chunkKey;
    }

    public NodeState getNodeState() {
        return nodeState;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }
}
