package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 每个节点的数据，NodeZone 加载时创建，存储在 ProgressCatalog 中
 */
public class NodeData {

    public static final String NODE_COLOR = "node_color";
    public static final String NODE_BLOCK_POS = "node_block_pos";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String NODE_STATE = "node_state";
    public static final String EVENTS = "events";
    public static final String CURRENT_EVENT_INDEX = "current_event_index";

    public static final Codec<NodeData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        NodeColor.CODEC.fieldOf(NODE_COLOR).forGetter(NodeData::getNodeColor),
        Codec.LONG.xmap(BlockPos::of, BlockPos::asLong).fieldOf(NODE_BLOCK_POS).forGetter(NodeData::getNodeBlockPos),
        EncounterType.CODEC.optionalFieldOf(ENCOUNTER_TYPE).forGetter(NodeData::getEncounterTypeOptional),
        NodeState.CODEC.optionalFieldOf(NODE_STATE, NodeState.LOCKED).forGetter(NodeData::getNodeState),
        NodeEventType.CODEC.listOf().optionalFieldOf(EVENTS, List.of()).forGetter(NodeData::getEvents),
        Codec.INT.optionalFieldOf(CURRENT_EVENT_INDEX, 0).forGetter(NodeData::getCurrentEventIndex)
    ).apply(builder, (nodeColor, nodeBlockPos, encounterType, nodeState, events, currentEventIndex) ->
        new NodeData(nodeColor, nodeBlockPos, encounterType.orElse(null), nodeState, events, currentEventIndex)
    ));

    private static final StreamCodec<RegistryFriendlyByteBuf, BlockPos> BLOCK_POS_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BlockPos decode(RegistryFriendlyByteBuf buf) {
            return BlockPos.of(buf.readLong());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, BlockPos blockPos) {
            buf.writeLong(blockPos.asLong());
        }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, Optional<EncounterType>> OPTIONAL_ENCOUNTER_STREAM_CODEC =
        ByteBufCodecs.optional(EncounterType.STREAM_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf, List<NodeEventType>> EVENT_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, NodeEventType.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeData> STREAM_CODEC = StreamCodec.composite(
        NodeColor.STREAM_CODEC,
        NodeData::getNodeColor,
        BLOCK_POS_STREAM_CODEC,
        NodeData::getNodeBlockPos,
        OPTIONAL_ENCOUNTER_STREAM_CODEC,
        NodeData::getEncounterTypeOptional,
        NodeState.STREAM_CODEC,
        NodeData::getNodeState,
        EVENT_LIST_STREAM_CODEC,
        NodeData::getEvents,
        ByteBufCodecs.VAR_INT,
        NodeData::getCurrentEventIndex,
        NodeData::new
    );

    /**
     * 每个节点生成的时候就固定了
     */
    private final NodeColor nodeColor;
    private final BlockPos nodeBlockPos;

    /**
     * 当玩家进入节点的时候，才会确定。
     */
    private EncounterType encounterType;
    private NodeState nodeState = NodeState.LOCKED;

    /**
     * roll 出来的事件列表，玩家需要依次触发
     */
    private List<NodeEventType> events = Collections.emptyList();

    /**
     * 当前执行到第几个事件
     */
    private int currentEventIndex = 0;

    public NodeData(NodeColor nodeColor, BlockPos nodeBlockPos) {
        this.nodeColor = nodeColor;
        this.nodeBlockPos = nodeBlockPos;
    }

    public NodeData(NodeColor nodeColor, BlockPos nodeBlockPos, EncounterType encounterType, NodeState nodeState, List<NodeEventType> events, int currentEventIndex) {
        this.nodeColor = nodeColor;
        this.nodeBlockPos = nodeBlockPos;
        this.encounterType = encounterType;
        this.nodeState = nodeState == null ? NodeState.LOCKED : nodeState;
        this.events = events == null ? new ArrayList<>() : new ArrayList<>(events);
        this.currentEventIndex = Math.max(0, currentEventIndex);
    }

    public NodeData(NodeColor nodeColor, BlockPos nodeBlockPos, Optional<EncounterType> encounterType, NodeState nodeState, List<NodeEventType> events, int currentEventIndex) {
        this(nodeColor, nodeBlockPos, encounterType.orElse(null), nodeState, events, currentEventIndex);
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
            nodeState = NodeState.COMPLETED;
            return false;
        }
        return true;
    }

    public NodeColor getNodeColor() {
        return nodeColor;
    }

    public BlockPos getNodeBlockPos() {
        return nodeBlockPos;
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
        return nodeState;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
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
