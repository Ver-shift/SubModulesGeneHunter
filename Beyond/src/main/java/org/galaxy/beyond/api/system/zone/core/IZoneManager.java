package org.galaxy.beyond.api.system.zone.core;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.concurrent.CompletableFuture;

public interface IZoneManager {

    void onChunkLoad(ChunkAccess chunk);

    boolean addZone(ServerLevel serverLevel, ChunkPos pos, ZoneType zoneType);

    void addSafeZone(ServerLevel serverLevel, int chunkSize, BlockPos center);

    void addNodeZone(ServerLevel serverLevel, BlockPos pos);

    CompletableFuture<NodeData> discoverNearestNode(ServerLevel serverLevel, ChunkPos center, int radius);

    void activeZoneInit(ServerLevel serverLevel);

    void addActiveZone(ServerLevel serverLevel, RogueNodeData nodeData);

    boolean expandFromWorldSeed(ServerLevel serverLevel, ChunkPos pos);

    default void tick(ServerLevel serverLevel) {
    }
}
