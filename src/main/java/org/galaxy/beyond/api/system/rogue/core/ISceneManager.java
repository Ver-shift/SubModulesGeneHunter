package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;

public interface ISceneManager {

    /**
     * 尝试步进
     * @param level
     */
    void nextScene(ServerLevel level);



}
