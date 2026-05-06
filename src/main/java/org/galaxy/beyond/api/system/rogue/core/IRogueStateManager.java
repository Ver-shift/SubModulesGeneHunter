package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;

/**
 * 肉鸽系统的生命周期管理器
 */
public interface IRogueStateManager {



    void tick(ServerLevel level);


    //辅助方法

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
}
