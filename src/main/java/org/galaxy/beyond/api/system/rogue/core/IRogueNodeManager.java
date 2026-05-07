package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;

public interface IRogueNodeManager {

    void tick(ServerLevel level);


    void handleOnEvent(ServerLevel level);

    /**
     * 设置肉鸽节点的状态，
     * @param level
     * @return
     */
    NodeState getNodeState(ServerLevel level);
    void setNodeState(ServerLevel level, NodeState state);

    RogueNodeData getRogueNodeData(ServerLevel level);
}
