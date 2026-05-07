package org.galaxy.beyond.api.system.rogue;

import lombok.AllArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueNodeManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueStateManager;
import org.galaxy.beyond.api.system.rogue.core.RogueState;
/**
 * 只负责执行状态，不负责切换状态。
 */

@AllArgsConstructor
public class RogueNodeManager implements IRogueNodeManager {
    private final IRogueManager rogueManager;

    @Override
    public void tick(ServerLevel level) {
        switch (getNodeState(level)){
            case LOCKED -> {}
            case PRE_NODE -> {}
            case NODE_INIT -> {handleNodeInit(level);}
            case ROGUE_PRE_EVENT -> {}
            case ON_EVENT_TASK -> {handleOnEvent(level);}
            case NODE_FINISH -> {handleNodeFinish(level);}
            case UNLOCKED -> {}
        }
    }

    @Override
    public void handleOnEvent(ServerLevel level) {
        //只会触发一个事件，就触发死锁，防止tick反复触发


    }

    public void handleNodeFinish(ServerLevel level) {
        //拓展区域，
        BeyondAPI.getBeyondManager().getZoneManager()
                .addActiveZone(level,getRogueNodeData(level));
        //步进一个Scene，并且锁定该节点.

        //或者结束对局。

    }
    public void handleNodeInit(ServerLevel level) {
        //抽取事件，填充RogueNodeData
    }

    @Override
    public NodeState getNodeState(ServerLevel level) {
        return null;
    }

    @Override
    public void setNodeState(ServerLevel level, NodeState state) {

    }

    @Override
    public RogueNodeData getRogueNodeData(ServerLevel level) {
        return null;
    }


}
