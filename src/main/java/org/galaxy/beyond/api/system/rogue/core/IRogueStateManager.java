package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.node.core.NodeState;

/**
 * 肉鸽系统的生命周期管理器
 */
public interface IRogueStateManager {



    void tick(ServerLevel level);


    //辅助方法====================================


    void setRogueState(ServerLevel level,RogueState state);
    RogueState getRogueState();

    /**
     * 玩家全部达到了这个方法
     * @param level
     * @return
     */
    PlayerRogueState isPlayerAllState(ServerLevel level);

    /**
     * 有一个玩家达成了这个State
     * @param level
     * @return
     */
    boolean hasPlayerRogueState(ServerLevel level,PlayerRogueState state);
    void setAllPlayerState(ServerLevel level,PlayerRogueState state);

    void setCurrentNodeState(ServerLevel level, NodeState state);
    NodeState getCurrentNodeState();
}
