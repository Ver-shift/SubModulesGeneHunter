package org.galaxy.beyond.api.system.rogue;

import lombok.AllArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueStateManager;
import org.galaxy.beyond.api.system.rogue.core.RogueState;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

@AllArgsConstructor
public class RogueStateManager implements IRogueStateManager {

    private final IRogueManager rogueStateManager;


    @Override
    public void tick(ServerLevel level) {



        //根据玩家的状态，更新肉鸽状态
        switch (isPlayerAllState(level)){
            case PRE_ROGUE -> setRogueState(level,RogueState.ROGUE_INIT);
            case PRE_NODE -> setRogueState(level,RogueState.NODE_INIT);

        }


        //根据状态分配不同的任务。如果是节点类型状态，那么就直接让节点处理
        switch (getRogueState(level)){
            case PRE_ROGUE -> {handlePreRogue(level);}
            case ROGUE_INIT -> {handleRogueInit(level);}
            case NODE_FINISH -> {handleNodeFinish(level);}
        }

        if (RogueState.isNodeEvent(getRogueState(level))){
            switch (getRogueState(level)) {
                case PRE_NODE -> setCurrentNodeState(level,NodeState.PRE_NODE);
                case NODE_INIT -> setCurrentNodeState(level,NodeState.NODE_INIT);
                case ROGUE_PRE_EVENT -> setCurrentNodeState(level,NodeState.ROGUE_PRE_EVENT);
                case ON_EVENT_TASK -> setCurrentNodeState(level,NodeState.ON_EVENT_TASK);
                case NODE_FINISH -> setCurrentNodeState(level,NodeState.NODE_FINISH);
            }
        }



    }

    @Override
    public void setRogueState(ServerLevel level, RogueState state) {

    }

    @Override
    public RogueState getRogueState() {
        return null;
    }

    public void handlePreRogue(ServerLevel level){
        //给节点也设置状态，具体逻辑用节点来跑
        //初始化节点，将定义数据转移
    }



    /**
     * 对当前节点进行完全初始化
     * @param level
     */
    public void handleRogueInit(ServerLevel level){
        //如果有玩家按下了多个节点。那就先抽取一个节点。
        //将这个节点进行事件抽取，多种抽取
        //设置节点的状态和自身的状态
        setRogueState(level,RogueState.ON_EVENT_TASK);
    }

    public void handleNodeFinish(ServerLevel level){
        //节点结束，清除部分数据，进行玩家可互动区域拓展。
        setRogueState(level,RogueState.ON_PROGRESS);
    }





    @Override
    public PlayerRogueState isPlayerAllState(ServerLevel level) {
        return null;
    }

    @Override
    public boolean hasPlayerRogueState(ServerLevel level, PlayerRogueState state) {
        return false;
    }

    @Override
    public void setAllPlayerState(ServerLevel level, PlayerRogueState state) {

    }

    @Override
    public void setCurrentNodeState(ServerLevel level, NodeState state) {

    }

    @Override
    public NodeState getCurrentNodeState() {
        return null;
    }

    private RogueState getRogueState(ServerLevel level) {
        return BeyondAPI.getBeyondLevelData(level).getRogueData().getRogueState();
    }
}
