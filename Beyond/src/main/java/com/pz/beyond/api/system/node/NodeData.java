package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 节点的静态数据，NodeZone 加载时创建。
 * 方案 C：基于 Structure 做 key，一个节点可以覆盖多个区块。
 * <p>
 * structureKey：节点唯一标识（结构 ResourceLocation + 结构中心 ChunkPos）。
 * chunks：该节点覆盖的所有区块（ChunkPos.toLong()）集合。
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NodeData {

    public static final String STRUCTURE_KEY = "structure_key";
    public static final String NODE_COLOR = "node_color";
    public static final String NODE_STATE = "node_state";
    public static final String CHUNKS = "chunks";


    private StructureKey structureKey = StructureKey.EMPTY;
    private NodeColor nodeColor = NodeColor.EMPTY;
    private NodeState nodeState = NodeState.LOCKED;
    private Set<Long> chunks = new HashSet<>();

    public void roll(){

    }

    private static final Codec<Set<Long>> CHUNKS_CODEC = Codec.LONG.listOf().xmap(
            HashSet::new,
            ArrayList::new
    );

    public static final Codec<NodeData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    StructureKey.CODEC.optionalFieldOf(STRUCTURE_KEY, StructureKey.EMPTY).forGetter(NodeData::getStructureKey),
                    NodeColor.CODEC.optionalFieldOf(NODE_COLOR, NodeColor.EMPTY).forGetter(NodeData::getNodeColor),
                    NodeState.CODEC.optionalFieldOf(NODE_STATE, NodeState.LOCKED).forGetter(NodeData::getNodeState),
                    CHUNKS_CODEC.optionalFieldOf(CHUNKS, new HashSet<>()).forGetter(NodeData::getChunks)
            ).apply(instance, NodeData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeData> STREAM_CODEC = StreamCodec.composite(
            StructureKey.STREAM_CODEC, NodeData::getStructureKey,
            NodeColor.STREAM_CODEC, NodeData::getNodeColor,
            NodeState.STREAM_CODEC, NodeData::getNodeState,
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.VAR_LONG), NodeData::getChunks,
            NodeData::new
    );
}
