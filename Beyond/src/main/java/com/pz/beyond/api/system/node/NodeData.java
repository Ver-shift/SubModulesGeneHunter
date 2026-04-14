package com.pz.beyond.api.system.node;

import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.List;

/**
 * 每个节点的数据，由nodeZone 节点区域生成的时候产生
 */
public class NodeData {

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
