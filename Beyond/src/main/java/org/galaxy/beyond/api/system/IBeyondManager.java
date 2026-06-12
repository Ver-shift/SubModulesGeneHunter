package org.galaxy.beyond.api.system;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.galaxy.beyond.api.system.node.core.INodeManager;
import org.galaxy.beyond.api.system.definition.IDefinitionManager;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueCapManager;
import org.galaxy.beyond.api.system.structure.core.ISafeZoneStructureManager;
import org.galaxy.beyond.api.system.structure.core.IStructureManager;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

public interface IBeyondManager {

    //system manager
    IZoneManager getZoneManager();
    INodeManager getNodeManager();
    IStructureManager getStructureManager();
    ISafeZoneStructureManager getSafeZoneStructureManager();
    RogueCapManager getRogueCapManager();
    IRogueContext getRogueContext();
    IDefinitionManager getDefinitionManager();

    //event handle
    void onLevelLoad(ServerLevel level);

    void levelTick(ServerLevel level);

    void playerTick(ServerPlayer player);

    void entityTick(LivingEntity entity);

    void playerChangedDimension(ServerPlayer player);

    void playerLoggedIn(ServerPlayer player);

    void onChunkLoad(LevelChunk chunk);

    void onServerStarted(MinecraftServer server);
}
