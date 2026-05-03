package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;

/**
 * 肉鸽系统扩展点，用于在 startRogue 生命周期中附加自定义逻辑。
 * 采用修饰器模式思想，每个扩展可在核心逻辑前后插入行为。
 */
public interface IRogueExtension {

    /**
     * 在 startRogue 时被调用，可在此添加前置/后置逻辑。
     *
     * @param level 当前生效的 ServerLevel
     */
    void onStartRogue(ServerLevel level);
}
