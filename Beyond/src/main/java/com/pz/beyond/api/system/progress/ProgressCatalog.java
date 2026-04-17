package com.pz.beyond.api.system.progress;

import com.pz.beyond.api.system.node.NodeData;
import com.pz.beyond.api.system.node.RolledData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 进度目录，玩家通过gui 选择当前的进度，获得相应的体验。
 */
@Data
public class ProgressCatalog {
    public static final String PROGRESS = "progress";
    public static final String NODE_DATA_MAP = "node_data_map";
    public static final String CURRENT_PROGRESS = "current_progress";
    public static final String PROGRESS_STATE = "progress_state";
    public static final String ACTIVE_ROLLED_DATA = "active_rolled_data";

    private static final Codec<List<Progress>> PROGRESS_LIST_CODEC = Progress.CODEC.listOf();
    private static final Codec<List<NodeData>> NODE_DATA_LIST_CODEC = NodeData.CODEC.listOf();

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Progress>> PROGRESS_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, Progress.STREAM_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf, List<NodeData>> NODE_DATA_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, NodeData.STREAM_CODEC);

    public static final Codec<ProgressCatalog> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        PROGRESS_LIST_CODEC.optionalFieldOf(PROGRESS, List.of()).forGetter(ProgressCatalog::getProgressList),
        NODE_DATA_LIST_CODEC.optionalFieldOf(NODE_DATA_MAP, List.of()).forGetter(ProgressCatalog::getNodeDataListForCodec),
        ResourceLocation.CODEC.optionalFieldOf(CURRENT_PROGRESS).forGetter(ProgressCatalog::getCurrentProgressId),
        ProgressState.CODEC.optionalFieldOf(PROGRESS_STATE, ProgressState.EMPTY)
            .forGetter(ProgressCatalog::getProgressStateOrDefault),
        RolledData.CODEC.optionalFieldOf(ACTIVE_ROLLED_DATA).forGetter(ProgressCatalog::getActiveRolledDataOptional)
    ).apply(builder, ProgressCatalog::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgressCatalog> STREAM_CODEC = StreamCodec.composite(
        PROGRESS_LIST_STREAM_CODEC,
        ProgressCatalog::getProgressList,
        NODE_DATA_LIST_STREAM_CODEC,
        ProgressCatalog::getNodeDataListForCodec,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        ProgressCatalog::getCurrentProgressId,
        ProgressState.STREAM_CODEC,
        ProgressCatalog::getProgressStateOrDefault,
        ByteBufCodecs.optional(RolledData.STREAM_CODEC),
        ProgressCatalog::getActiveRolledDataOptional,
        ProgressCatalog::new
    );

    /**
     * ResourceLocation - progressType
     */
    private Map<ResourceLocation, Progress> progress = new HashMap<>();
    private Progress currentProgress;

    /**
     * 节点静态数据，key 为 chunkKey (ChunkPos.toLong())，NodeZone 加载时创建并存入
     */
    private Map<Long, NodeData> nodeDataMap = new HashMap<>();

    /**
     * 当前的关卡状态
     */
    private ProgressState progressState;

    /**
     * 当前激活的节点运行时数据，避免每次遍历
     */
    private RolledData activeRolledData;

    public ProgressCatalog() {
    }

    public ProgressCatalog(List<Progress> progressList, List<NodeData> nodeDataList,
                           Optional<ResourceLocation> currentProgressId, ProgressState progressState,
                           Optional<RolledData> activeRolledData) {
        if (progressList != null) {
            progressList.forEach(progressData -> progress.put(progressData.getTypeId(), progressData));
        }
        if (nodeDataList != null) {
            nodeDataList.forEach(nd -> nodeDataMap.put(nd.getChunkKey(), nd));
        }
        Optional<ResourceLocation> safeCurrentProgressId = currentProgressId == null ? Optional.empty() : currentProgressId;
        this.currentProgress = safeCurrentProgressId.map(id -> progress.computeIfAbsent(id, unused -> new Progress())).orElse(null);
        this.progressState = progressState == null ? ProgressState.EMPTY : progressState;
        this.activeRolledData = activeRolledData == null ? null : activeRolledData.orElse(null);
    }

    // --- 节点数据操作 ---

    public void putNodeData(NodeData nodeData) {
        nodeDataMap.put(nodeData.getChunkKey(), nodeData);
    }

    public NodeData getNodeData(long chunkKey) {
        return nodeDataMap.get(chunkKey);
    }

    // --- Codec 辅助方法 ---

    private List<Progress> getProgressList() {
        return progress.entrySet().stream()
            .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
            .map(Map.Entry::getValue)
            .toList();
    }

    private Optional<ResourceLocation> getCurrentProgressId() {
        return progress.entrySet().stream()
            .filter(entry -> entry.getValue() == currentProgress)
            .map(Map.Entry::getKey)
            .findFirst();
    }

    private ProgressState getProgressStateOrDefault() {
        return progressState == null ? ProgressState.EMPTY : progressState;
    }

    /**
     * 序列化时将 Map 转为 List
     */
    private List<NodeData> getNodeDataListForCodec() {
        return new ArrayList<>(nodeDataMap.values());
    }

    private Optional<RolledData> getActiveRolledDataOptional() {
        return Optional.ofNullable(activeRolledData);
    }

    // --- 进度管理方法 ---

    public void addProgress(Progress progressData) {
        if (progressData != null && progressData.getType() != null) {
            progress.put(progressData.getType().getId(), progressData);
        }
    }

    public void setCurrentProgressById(ResourceLocation id) {
        this.currentProgress = progress.get(id);
    }

    public void startGame() {
        // TODO: 实现游戏开始逻辑，预生成节点事件等
        if (currentProgress != null) {
            currentProgress.initialize();
        }
    }
}
