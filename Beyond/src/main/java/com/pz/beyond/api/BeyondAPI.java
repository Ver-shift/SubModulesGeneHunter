package com.pz.beyond.api;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.BeyondManager;
import com.pz.beyond.api.system.BeyondPlayerData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Beyond 模块 API 入口
 */
public class BeyondAPI {



    public static BeyondLevelData getBeyondLevelData(Level level) {
        return level.getData(BeyondAttachInit.LEVEL_DATA);
    }



    public static BeyondPlayerData getBeyondPlayerData(Player player) {
        return player.getData(BeyondAttachInit.BEYOND_DATA);
    }

    public static BeyondManager getBeyondManager() {
        return Beyond.BEYOND_MANAGER;
    }


}
