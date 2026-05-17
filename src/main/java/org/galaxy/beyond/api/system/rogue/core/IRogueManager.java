package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.ProgressManager;

/**
 * 以点带面回点 玩家状态->肉鸽状态->节点状态
 */
public interface IRogueManager {

    /**
     * 设置当前肉鸽系统在哪个维度生效
     */
    void setRogueLevel(ResourceKey<Level> level);

    /**
     * 系统总线，每tick检测条件 → 自动推进状态。
     */
    void tick(ServerLevel level);

    IRogueStateManager getRogueStateManager();
    IPlayerRougeManager getPlayerRougeManager();
    IRogueNodeManager getRogueNodeManager();

    ProgressManager getProgressManager();

}
