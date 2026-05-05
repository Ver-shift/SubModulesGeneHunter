package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueState;

/**
 * 肉鸽系统的生命周期管理器
 */
public interface IRogueStateManager {



    void tick(ServerLevel level);

    PlayerRogueState isPlayerAllState(ServerLevel level);
}
