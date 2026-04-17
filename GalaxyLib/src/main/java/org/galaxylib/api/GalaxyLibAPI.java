package org.galaxylib.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.galaxylib.api.init.GalaxyLibAttachInit;
import org.galaxylib.api.init.GalaxyLibCapInit;
import org.galaxylib.api.system.loot.PlayerLootTableData;
import org.galaxylib.api.system.loot.core.ILootTableManager;
import org.galaxylib.api.system.random.RandomManager;

public class GalaxyLibAPI {


    public static PlayerLootTableData getPlayerLootTableData(ServerPlayer player) {
        return player.getData(GalaxyLibAttachInit.PLAYER_LOOT_TABLE_DATA);
    }
    public static ILootTableManager getLootTableManager(ServerPlayer player) {
        return player.getCapability(GalaxyLibCapInit.LOOT_TABLE);
    }

    public static RandomManager getRandomManager(Level level) {
        return level.getData(GalaxyLibAttachInit.RANDOM_MANAGER);
    }
}
