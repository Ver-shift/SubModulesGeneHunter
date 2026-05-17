package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueNodeManager;

/**
 * 只负责执行节点逻辑，不负责状态切换。状态切换由 PhaseRunner + IRoguePhase 管理。
 */
public class RogueNodeManager implements IRogueNodeManager {

    private final IRogueManager rogueManager;

    public RogueNodeManager(IRogueManager rogueManager) {
        this.rogueManager = rogueManager;
    }

    @Override
    public void tick(ServerLevel level) {
        NodeState state = getNodeState(level);
        switch (state) {
            case PRE_EVENT -> handlePreEvent(level);
            case ON_EVENT -> handleOnEvent(level);
            default -> { /* 无操作 */ }
        }
    }

    @Override
    public void handleOnEvent(ServerLevel level) {
        // 触发事件，由 PhaseRunner 驱动
    }

    @Override
    public void handlePreEvent(ServerLevel level) {
        // 抽取遭遇类型，填充 RogueNodeData
    }

    @Override
    public NodeState getNodeState(ServerLevel level) {
        RogueNodeData data = getRogueNodeData(level);
        if (data == null || data.getNodeData() == null) return NodeState.LOCKED;
        return data.getNodeData().getState();
    }

    @Override
    public void setNodeState(ServerLevel level, NodeState state) {
        RogueNodeData data = getOrInitRogueNodeData(level);
        if (data.getNodeData() == null) {
            data.setNodeData(new org.galaxy.beyond.api.system.node.NodeData());
        }
        data.getNodeData().setState(state);
    }

    private RogueNodeData getOrInitRogueNodeData(ServerLevel level) {
        var rogueData = BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData();
        if (rogueData.getRogueNodeData() == null) {
            rogueData.setRogueNodeData(new RogueNodeData());
        }
        return rogueData.getRogueNodeData();
    }

    @Override
    public RogueNodeData getRogueNodeData(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData().getRogueNodeData();
    }
}
