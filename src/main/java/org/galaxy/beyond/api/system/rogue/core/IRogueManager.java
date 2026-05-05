package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueState;

public interface IRogueManager {

    /**
     * 设置当前肉鸽系统在哪个维度生效
     */
    void setRogueLevel(ResourceKey<Level> level);

    /**
     * 系统总线，每tick检测条件 → 自动推进状态。
     * 不执行具体业务逻辑，业务逻辑由 NeoForge Event 承载。
     */
    void tick(ServerLevel level);

    IRogueStateManager getRogueStateManager();
    IPlayerRougeManager getPlayerRougeManager();
    


}
