package org.galaxy.beyond.api.system;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.BeyondAttachmentInit;
import org.galaxy.beyond.api.system.large.BeyondLargeLevelData;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueData;
import org.galaxy.beyond.api.system.rogue.ProgressType;
import org.galaxy.beyond.api.system.rogue.definition.RogueDefinition;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureData;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

import java.util.List;

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

    public static BeyondDimensionData getRogueDimensionData(Level level) {
        return getBeyondDimensionData(level, CommonConfig.getRogueDimension());
    }

    public static RogueData getRogueData(Level level) {
        return getRogueDimensionData(level).getRogueData();
    }

    public static LevelZoneData getLevelZoneData(Level level) {
        return getLargeLevelData(level).getLevelZoneData();
    }

    public static BeyondLargeLevelData getLargeLevelData(Level level) {
        return level.getData(BeyondAttachmentInit.LARGE_LEVEL_DATA.get());
    }

    public static List<NodeData> getNodeDatas(Level level) {
        return getLargeLevelData(level).getNodeDatas();
    }

    public static NodeData findNodeData(Level level, net.minecraft.world.level.ChunkPos pos) {
        return getLargeLevelData(level).findNodeData(pos);
    }

    public static SafeZoneStructureData getSafeZoneStructureData(Level level) {
        return getBeyondDimensionData(level).getSafeZoneStructureData();
    }

    public static ProgressType getProgressType(Level level) {
        return getRogueData(level).getProgressType();
    }

    public static RogueDefinition getRogueDefinition(Level level) {
        return getGlobalData(level).getRogueDefinition();
    }

    public static BeyondPlayerData getBeyondPlayerData(Player player) {
        return player.getData(BeyondAttachmentInit.PLAYER_DATA.get());
    }

    public static BeyondMobData getBeyondMobData(LivingEntity entity) {
        return entity.getData(BeyondAttachmentInit.MOB_DATA.get());
    }

    public static void syncGlobalData(ServerLevel level) {
        level.syncData(BeyondAttachmentInit.GLOBAL_DATA.get());
    }

    public static void syncLargeLevelData(ServerLevel level) {
        getLargeLevelData(level).prepareSyncSnapshot();
        level.syncData(BeyondAttachmentInit.LARGE_LEVEL_DATA.get());
    }

    public static void syncPlayerData(ServerPlayer player) {
        player.syncData(BeyondAttachmentInit.PLAYER_DATA.get());
    }

}
