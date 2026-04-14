package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 节点的运行时数据，玩家交互节点后产生。
 * 包含对 NodeData 的引用以及 roll 出来的事件等运行时状态。
 */
public class RolledData {

    public static final String NODE_DATA = "node_data";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String EVENTS = "events";
    public static final String CURRENT_EVENT_INDEX = "current_event_index";

    public static final Codec<RolledData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        NodeData.CODEC.fieldOf(NODE_DATA).forGetter(RolledData::getNodeData),
        EncounterType.CODEC.optionalFieldOf(ENCOUNTER_TYPE).forGetter(RolledData::getEncounterTypeOptional),
        NodeEventType.CODEC.listOf().optionalFieldOf(EVENTS, List.of()).forGetter(RolledData::getEvents),
        Codec.INT.optionalFieldOf(CURRENT_EVENT_INDEX, 0).forGetter(RolledData::getCurrentEventIndex)
    ).apply(builder, (nodeData, encounterType, events, currentEventIndex) ->
        new RolledData(nodeData, encounterType.orElse(null), events, currentEventIndex)
    ));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<NodeEventType>> EVENT_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, NodeEventType.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, RolledData> STREAM_CODEC = StreamCodec.composite(
        NodeData.STREAM_CODEC,
        RolledData::getNodeData,
        ByteBufCodecs.optional(EncounterType.STREAM_CODEC),
        RolledData::getEncounterTypeOptional,
        EVENT_LIST_STREAM_CODEC,
        RolledData::getEvents,
        ByteBufCodecs.VAR_INT,
        RolledData::getCurrentEventIndex,
        RolledData::new
    );

    /**
     * 关联的静态节点数据
     */
    private final NodeData nodeData;

    /**
     * 遭遇类型，玩家进入节点时确定
     */
    private EncounterType encounterType;
    
    /**
     * roll 出来的事件列表，玩家需要依次触发
     */
    private List<NodeEventType> events = Collections.emptyList();

    /**
     * 当前执行到第几个事件
     */
    private int currentEventIndex = 0;

    public RolledData(NodeData nodeData) {
        this.nodeData = nodeData;
    }

    public RolledData(NodeData nodeData, EncounterType encounterType, List<NodeEventType> events, int currentEventIndex) {
        this.nodeData = nodeData;
        this.encounterType = encounterType;
        this.events = events == null ? new ArrayList<>() : new ArrayList<>(events);
        this.currentEventIndex = Math.max(0, currentEventIndex);
    }

    public RolledData(NodeData nodeData, Optional<EncounterType> encounterType, List<NodeEventType> events, int currentEventIndex) {
        this(nodeData, encounterType.orElse(null), events, currentEventIndex);
    }

    /**
     * 获取当前正在执行的事件
     */
    public NodeEventType getCurrentEvent() {
        if (events.isEmpty() || currentEventIndex >= events.size()) {
            return null;
        }
        return events.get(currentEventIndex);
    }

    /**
     * 推进到下一个事件
     * @return true 如果还有下一个事件，false 如果已全部完成
     */
    public boolean advanceEvent() {
        currentEventIndex++;
        if (currentEventIndex >= events.size()) {
            nodeData.setNodeState(NodeState.COMPLETED);
            return false;
        }
        return true;
    }

    // --- Getters & Setters ---

    public NodeData getNodeData() {
        return nodeData;
    }

    public long getChunkKey() {
        return nodeData.getChunkKey();
    }

    public NodeColor getNodeColor() {
        return nodeData.getNodeColor();
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    private Optional<EncounterType> getEncounterTypeOptional() {
        return Optional.ofNullable(encounterType);
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public NodeState getNodeState() {
        return nodeData.getNodeState();
    }

    public void setNodeState(NodeState nodeState) {
        nodeData.setNodeState(nodeState);
    }

    public List<NodeEventType> getEvents() {
        return events;
    }

    public void setEvents(List<NodeEventType> events) {
        this.events = events;
        this.currentEventIndex = 0;
    }

    public int getCurrentEventIndex() {
        return currentEventIndex;
    }
}
