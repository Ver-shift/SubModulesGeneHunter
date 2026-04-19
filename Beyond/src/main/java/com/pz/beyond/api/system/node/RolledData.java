package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.init.BeyondEncounters;
import com.pz.beyond.api.init.BeyondNodeEventTypes;
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


    private final NodeData nodeData;


    private EncounterType encounterType = BeyondEncounters.EMPTY;

    private List<NodeEventType> events = Collections.emptyList();
    
    private int currentEventIndex = 0;

    public RolledData(NodeData nodeData) {
        this.nodeData = nodeData;
    }

    public RolledData(NodeData nodeData, EncounterType encounterType, List<NodeEventType> events, int currentEventIndex) {
        this.nodeData = nodeData;
        this.encounterType = encounterType == null ? BeyondEncounters.EMPTY : encounterType;
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
            return BeyondNodeEventTypes.EMPTY;
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
        this.encounterType = encounterType == null ? BeyondEncounters.EMPTY : encounterType;
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
        this.events = events == null ? new ArrayList<>() : events;
        this.currentEventIndex = 0;
    }

    public int getCurrentEventIndex() {
        return currentEventIndex;
    }
}
