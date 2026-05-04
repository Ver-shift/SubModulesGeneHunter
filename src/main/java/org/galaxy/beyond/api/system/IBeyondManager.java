package org.galaxy.beyond.api.system;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.galaxy.beyond.api.system.node.core.INodeManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.structure.core.IStructureManager;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

public interface IBeyondManager {

    //system manager
    IZoneManager getZoneManager();
    INodeManager getNodeManager();
    IRogueManager getRogueManager();
    IStructureManager getStructureManager();

    //event handle
    void levelTick(ServerLevel level);

    void playerTick(ServerPlayer player);

    void entityTick(LivingEntity entity);

    void playerLogin(ServerPlayer player);

    void onChunkLoad(LevelChunk chunk);
}
