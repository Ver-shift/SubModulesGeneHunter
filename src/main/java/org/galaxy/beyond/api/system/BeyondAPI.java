package org.galaxy.beyond.api.system;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondAttachmentInit;

public class BeyondAPI {

    public static IBeyondManager getBeyondManager() {
        return Beyond.MANAGER;
    }

    public static ServerLevel getOverWorld() {
        return Beyond.OVERWORLD;
    }

    public static MinecraftServer getOverMinecraftServer() {
        return Beyond.SERVER;
    }

    public static BeyondGlobalData getGlobalData(Level level) {
        return level.getData(BeyondAttachmentInit.GLOBAL_DATA.get());
    }

    public static BeyondDimensionData getBeyondDimensionData(Level level, ResourceKey<Level> dimension) {
        return getGlobalData(level).getOrCreateDimensionData(dimension);
    }

    public static BeyondDimensionData getBeyondDimensionData(Level level) {
        return getBeyondDimensionData(level, level.dimension());
    }

    public static BeyondPlayerData getBeyondPlayerData(ServerPlayer player) {
        return player.getData(BeyondAttachmentInit.PLAYER_DATA.get());
    }

    public static BeyondMobData getBeyondMobData(LivingEntity entity) {
        return entity.getData(BeyondAttachmentInit.MOB_DATA.get());
    }



}
