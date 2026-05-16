package org.galaxy.beyond.api.system.structure.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface ISafeZoneStructureManager {

    void initialize(ServerLevel level);

    void onPlayerEnterDimension(ServerPlayer player);

    void trySafeZoneSpawn(ServerPlayer player);

    void playerTick(ServerPlayer player);
}
