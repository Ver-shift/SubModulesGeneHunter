package org.galaxy.beyond.api.system.zone.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.ZoneType;

public interface IZoneManager {

    void onChunkLoad(ChunkAccess chunk);

    boolean addZone(ServerLevel serverLevel, ChunkPos pos, ZoneType zoneType);

    void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center);
    void addNodeZone(ServerLevel serverLevel, BlockPos pos);

    void activeZoneInit(ServerLevel serverLevel);
    void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData);
}
