package com.pz.beyond.api.system;

import com.pz.beyond.api.system.rule.IZoneRule;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public interface IBeyondManager {


    void levelTick(Level level);

    void loadLevel(ServerLevel serverLevel);

    void playerFirstLoad(ServerPlayer serverPlayer);

    /**
     * 区块首次加载时由事件层转发过来，用于触发 {@code spawnZone}。
     */
    void onChunkLoad(ServerLevel level, ChunkPos chunkPos);

}