package com.pz.beyond.api.system.progress;

import com.pz.beyond.api.system.node.NodeData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 进度目录，玩家通过gui 选择当前的进度，获得相应的体验。
 */
@Data
public class ProgressCatalog {
    public static final String PROGRESS = "progress";
    public static final String NODE_DATA_LIST = "node_data_list";
    public static final String CURRENT_PROGRESS = "current_progress";
    public static final String PROGRESS_STATE = "progress_state";

    private static final Codec<List<Progress>> PROGRESS_LIST_CODEC = Progress.CODEC.listOf();
    private static final Codec<List<NodeData>> NODE_DATA_LIST_CODEC = NodeData.CODEC.listOf();

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Progress>> PROGRESS_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, Progress.STREAM_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf, List<NodeData>> NODE_DATA_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, NodeData.STREAM_CODEC);

    public static final Codec<ProgressCatalog> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        PROGRESS_LIST_CODEC.optionalFieldOf(PROGRESS, List.of()).forGetter(ProgressCatalog::getProgressList),
        NODE_DATA_LIST_CODEC.optionalFieldOf(NODE_DATA_LIST, List.of()).forGetter(ProgressCatalog::getNodeDataListSafe),
        ResourceLocation.CODEC.optionalFieldOf(CURRENT_PROGRESS).forGetter(ProgressCatalog::getCurrentProgressId),
        ProgressState.CODEC.optionalFieldOf(PROGRESS_STATE, ProgressState.EMPTY)
            .forGetter(ProgressCatalog::getProgressStateOrDefault)
    ).apply(builder, ProgressCatalog::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgressCatalog> STREAM_CODEC = StreamCodec.composite(
        PROGRESS_LIST_STREAM_CODEC,
        ProgressCatalog::getProgressList,
        NODE_DATA_LIST_STREAM_CODEC,
        ProgressCatalog::getNodeDataListSafe,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        ProgressCatalog::getCurrentProgressId,
        ProgressState.STREAM_CODEC,
        ProgressCatalog::getProgressStateOrDefault,
        ProgressCatalog::new
    );

    /**
     * ResourceLocation - progressType
     */
    private Map<ResourceLocation, Progress> progress = new HashMap<>();
    private Progress currentProgress;

    /**
     * 当前关卡的所有节点数据，NodeZone 加载时创建并存入
     */
    private List<NodeData> nodeDataList = new ArrayList<>();

    /**
     * 当前的关卡状态
     */
    private ProgressState progressState;

    public ProgressCatalog() {
    }

    public ProgressCatalog(List<Progress> progressList, List<NodeData> nodeDataList, Optional<ResourceLocation> currentProgressId, ProgressState progressState) {
        if (progressList != null) {
            progressList.forEach(progressData -> progress.put(progressData.getTypeId(), progressData));
        }
        this.nodeDataList = nodeDataList == null ? new ArrayList<>() : new ArrayList<>(nodeDataList);
        Optional<ResourceLocation> safeCurrentProgressId = currentProgressId == null ? Optional.empty() : currentProgressId;
        this.currentProgress = safeCurrentProgressId.map(id -> progress.computeIfAbsent(id, unused -> new Progress())).orElse(null);
        this.progressState = progressState == null ? ProgressState.EMPTY : progressState;
    }

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

    private List<NodeData> getNodeDataListSafe() {
        return nodeDataList == null ? List.of() : nodeDataList;
    }
}
