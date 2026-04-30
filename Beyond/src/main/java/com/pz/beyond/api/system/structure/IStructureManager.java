package com.pz.beyond.api.system.structure;

import net.minecraft.server.level.ServerLevel;

public interface IStructureManager {

    /**
     * 创建安全区
     * @param level
     */
    void findSafeZone(ServerLevel level);


}
