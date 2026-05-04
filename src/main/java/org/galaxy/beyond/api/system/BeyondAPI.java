package org.galaxy.beyond.api.system;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.galaxy.beyond.Beyond;

public class BeyondAPI {

    public static IBeyondManager getBeyondManager() {
        return Beyond.MANAGER;
    }

    public static ServerLevel getOverWorld(){
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            return ServerLifecycleHooks.getCurrentServer().overworld();
        }
        return null;
    }

    public static BeyondGlobalData getGlobalData(MinecraftServer server) {
        return null;

    }
    public static BeyondLevelData getBeyondLevelData(ServerLevel level) {
        return null;
    }
    public static BeyondPlayerData getBeyondPlayerData(ServerPlayer player) {
        return null;
    }


}
