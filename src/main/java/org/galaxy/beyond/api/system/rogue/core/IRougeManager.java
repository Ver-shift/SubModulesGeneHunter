package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.RogueState;

public interface IRougeManager {

    /**
     * 设置当前肉鸽系统在哪里生效
     */
    void setRogueLevel(ResourceKey<Level> level);

    /**
     * 系统总线，
     * @param level
     */
    void tick(ServerLevel level);

    /**
     * 尝试启动游戏，当每个玩家的游戏状态达到ready，就能启动
     * @param level
     */
    void tryStratRogue(ServerLevel level);

    /**
     * 采用修饰器模式，进行附加逻辑添加，
     * @param level
     */
    void startRogue(ServerLevel level);
    /**
     * 尝试启动节点，每个玩家都需要Ready状态。
     * @param level
     */
    void tryStratNode(ServerLevel level);

    void tryFinishRogue(ServerLevel level);

    /**
     * 注册内置扩展（修饰器模式），仅在指定状态下触发。
     * 适用于同模组内部的轻量逻辑组合。
     */
    void addExtension(RogueState rogueState, IRogueExtension extension);

    void removeExtension(RogueState rogueState, IRogueExtension extension);

    void clearExtensions(RogueState rogueState);
}
