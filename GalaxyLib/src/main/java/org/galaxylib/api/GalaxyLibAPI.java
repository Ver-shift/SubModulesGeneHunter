package org.galaxylib.api;

import net.minecraft.world.level.Level;
import org.galaxylib.GalaxyLib;
import org.galaxylib.api.init.GalaxyLibAttachInit;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.random.RandomManager;

public class GalaxyLibAPI {
    public static LootManager getLootManager() {
        return GalaxyLib.MANAGER.getLootManager();
    }

    public static RandomManager getRandomManager(Level level) {
        return level.getData(GalaxyLibAttachInit.RANDOM_MANAGER);
    }
}
