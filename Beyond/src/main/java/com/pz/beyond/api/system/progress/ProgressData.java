package com.pz.beyond.api.system.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.node.NodeData;
import com.pz.beyond.api.system.node.RolledData;
import com.pz.beyond.api.system.node.StructureKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 当前关卡运行时数据（方案 C：基于 Structure 做 key）。
 * <p>
 * nodes：{@link StructureKey} → {@link NodeData}；一个节点可对应多个区块，区块 → StructureKey
 * 反查由 {@link #chunkIndex} 维护（transient，反序列化后调用 {@link #rebuildChunkIndex()} 重建）。
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProgressData {

    public static final String SCENE_TYPES = "scene_types";
    public static final String INDEX = "index";
    public static final String CURRENT_PROGRESS = "current_progress";
    public static final String NODES = "nodes";
    public static final String CURRENT_NODE_DATA = "current_node_data";


    //运行时 scene 数据，于进度条相关
    private List<SceneType> sceneTypes = new LinkedList<>();
    //index 数据，查看当前的进度。
    private int index = 0;
    //当前的关卡数据。
    private ResourceLocation currentProgress;
    //缓存的节点数据：StructureKey(结构 id + 中心 ChunkPos) → NodeData
    private HashMap<StructureKey, NodeData> nodes = new HashMap<>();

    //当前节点的触发数据。动态填充，单例模式
    private RolledData currentNodeData = new RolledData();

    //todo 后期统计数据，记录玩家的造成的伤害之类的。


    //==================== 区块 → 节点 反查索引（不入 Codec） ====================
    /**
     * 区块 key(ChunkPos.toLong()) → StructureKey 反查索引。
     * 仅运行时使用，反序列化后通过 {@link #rebuildChunkIndex()} 从 {@link #nodes} 重建。
     */
    private transient HashMap<Long, StructureKey> chunkIndex = new HashMap<>();

    /**
     * 通过区块 key 反查该区块所属节点。
     */
    public NodeData getNodeByChunk(long chunkKey) {
        StructureKey key = chunkIndex.get(chunkKey);
        return key == null ? null : nodes.get(key);
    }

    /**
     * 通过区块 key 反查节点的 StructureKey。
     */
    public StructureKey getStructureKeyByChunk(long chunkKey) {
        return chunkIndex.get(chunkKey);
    }

    /**
     * 注册节点：写入 nodes 并同步 chunkIndex 反查索引。
     */
    public void registerNode(NodeData data) {
        if (data == null || data.getStructureKey() == null) return;
        StructureKey key = data.getStructureKey();
        nodes.put(key, data);
        Set<Long> chunks = data.getChunks();
        if (chunks == null) return;
        for (Long c : chunks) {
            chunkIndex.put(c, key);
        }
    }

    /**
     * 从 nodes 里扁平化重建 chunkIndex。反序列化完成后必须调用一次。
     */
    public void rebuildChunkIndex() {
        HashMap<Long, StructureKey> index = new HashMap<>();
        for (Map.Entry<StructureKey, NodeData> e : nodes.entrySet()) {
            NodeData nd = e.getValue();
            if (nd == null || nd.getChunks() == null) continue;
            StructureKey key = e.getKey();
            for (Long c : nd.getChunks()) {
                index.put(c, key);
            }
        }
        this.chunkIndex = index;
    }


    //==================== Codec ====================
    // StructureKey 通过 STRING_CODEC 做 Map key（格式：namespace:path@cx,cz）
    private static final Codec<HashMap<StructureKey, NodeData>> NODES_CODEC = Codec.unboundedMap(
            StructureKey.STRING_CODEC,
            NodeData.CODEC
    ).xmap(HashMap::new, HashMap::new);

    public static final Codec<ProgressData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SceneType.CODEC.listOf().optionalFieldOf(SCENE_TYPES, new ArrayList<>()).forGetter(p -> new ArrayList<>(p.getSceneTypes())),
                    Codec.INT.optionalFieldOf(INDEX, 0).forGetter(ProgressData::getIndex),
                    ResourceLocation.CODEC.optionalFieldOf(CURRENT_PROGRESS).forGetter(p -> java.util.Optional.ofNullable(p.getCurrentProgress())),
                    NODES_CODEC.optionalFieldOf(NODES, new HashMap<>()).forGetter(ProgressData::getNodes),
                    RolledData.CODEC.optionalFieldOf(CURRENT_NODE_DATA, new RolledData()).forGetter(ProgressData::getCurrentNodeData)
            ).apply(instance, ProgressData::fromCodec)
    );

    private static ProgressData fromCodec(List<SceneType> scenes, int index,
                                          java.util.Optional<ResourceLocation> currentProgress,
                                          HashMap<StructureKey, NodeData> nodes, RolledData currentNodeData) {
        ProgressData data = new ProgressData();
        data.setSceneTypes(new LinkedList<>(scenes));
        data.setIndex(index);
        data.setCurrentProgress(currentProgress.orElse(null));
        data.setNodes(nodes == null ? new HashMap<>() : nodes);
        data.setCurrentNodeData(currentNodeData == null ? new RolledData() : currentNodeData);
        data.rebuildChunkIndex();
        return data;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgressData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ProgressData decode(RegistryFriendlyByteBuf buf) {
            ProgressData data = new ProgressData();
            // sceneTypes
            data.setSceneTypes(new LinkedList<>(ByteBufCodecs.collection(ArrayList::new, SceneType.STREAM_CODEC).decode(buf)));
            // index
            data.setIndex(ByteBufCodecs.VAR_INT.decode(buf));
            // currentProgress（optional）
            boolean hasCurrent = buf.readBoolean();
            data.setCurrentProgress(hasCurrent ? ResourceLocation.STREAM_CODEC.decode(buf) : null);
            // nodes
            HashMap<StructureKey, NodeData> nodes = ByteBufCodecs.map(HashMap::new, StructureKey.STREAM_CODEC, NodeData.STREAM_CODEC).decode(buf);
            data.setNodes(nodes);
            // currentNodeData
            data.setCurrentNodeData(RolledData.STREAM_CODEC.decode(buf));
            // 重建反查索引
            data.rebuildChunkIndex();
            return data;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ProgressData data) {
            ByteBufCodecs.collection(ArrayList::new, SceneType.STREAM_CODEC).encode(buf, new ArrayList<>(data.getSceneTypes()));
            ByteBufCodecs.VAR_INT.encode(buf, data.getIndex());
            ResourceLocation cp = data.getCurrentProgress();
            buf.writeBoolean(cp != null);
            if (cp != null) ResourceLocation.STREAM_CODEC.encode(buf, cp);
            ByteBufCodecs.map(HashMap::new, StructureKey.STREAM_CODEC, NodeData.STREAM_CODEC).encode(buf, data.getNodes());
            RolledData.STREAM_CODEC.encode(buf, data.getCurrentNodeData() == null ? new RolledData() : data.getCurrentNodeData());
        }
    };

}
