package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 节点的静态数据，NodeZone 加载时创建，存储在 ProgressCatalog 中。
 * 只包含节点生成时就固定的信息。
 * chunkKey 为 ChunkPos.toLong()，与 Zone 系统保持一致。
 */
public class NodeData {

    public static final String NODE_COLOR = "node_color";
    public static final String CHUNK_KEY = "chunk_key";
    public static final String NODE_STATE = "node_state";


    private final NodeColor nodeColor;
    private final long chunkKey;
    private NodeState nodeState = NodeState.LOCKED;

    public NodeData(NodeColor nodeColor, long chunkKey) {
        this.nodeColor = nodeColor == null ? NodeColor.EMPTY : nodeColor;
        this.chunkKey = chunkKey;
    }

    public NodeData(NodeColor nodeColor, long chunkKey, NodeState nodeState) {
        this.nodeColor = nodeColor == null ? NodeColor.EMPTY : nodeColor;
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
        this.nodeState = nodeState == null ? NodeState.LOCKED : nodeState;
    }
}
