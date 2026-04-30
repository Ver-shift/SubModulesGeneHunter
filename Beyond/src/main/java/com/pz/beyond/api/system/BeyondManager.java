package com.pz.beyond.api.system;

import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.progress.ProgressManager;
import com.pz.beyond.api.system.structure.PlayerStructureManager;
import com.pz.beyond.api.system.structure.StructureManager;
import com.pz.beyond.api.system.zone.SafeZoneManager;
import com.pz.beyond.api.system.zone.ZoneManager;
import com.pz.beyond.api.system.zone.ZonePosManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * Level-scoped runtime manager that dispatches zone rules.
 */
@NoArgsConstructor
public class BeyondManager implements IBeyondManager {

    @Getter
    private final ZoneManager zoneManager = new ZoneManager();
    @Getter
    private final StructureManager structureManager = new StructureManager();
    @Getter
    private final ZonePosManager zonePosManager = new ZonePosManager();
    @Getter
    private final SafeZoneManager safeZoneManager = new SafeZoneManager();

    private final PlayerStructureManager structureManagerPlayer = new PlayerStructureManager();

    @Getter
    private final ProgressManager progressManager = new ProgressManager();
    @Override
    public void levelTick(Level level) {
        if (level instanceof ServerLevel serverLevel && BeyondAttachInit.isAllowedDimension(serverLevel)) {
            zoneManager.handleZoneRule(serverLevel);

        }
    }

    @Override
    public void loadLevel(ServerLevel serverLevel) {
        if (!BeyondAttachInit.isAllowedDimension(serverLevel)) {
            return;
        }
        structureManager.findSafeZone(serverLevel);
        // 以村庄（StructureData.spawnPos）为中心铺设初始 SafeZone，再向外扩展一圈 PlayerActiveZone。
        zonePosManager.addSafeZone(serverLevel, ZonePosManager.DEFAULT_SAFE_CHUNK_SIZE);
        zonePosManager.addPlayerActiveZone(serverLevel);
    }

    @Override
    public void playerFirstLoad(ServerPlayer serverPlayer) {
        structureManagerPlayer.handlePlayerFirstLoad(serverPlayer);
    }

    @Override
    public void onChunkLoad(ServerLevel level, ChunkPos chunkPos) {
        if (BeyondAttachInit.isAllowedDimension(level)) {
            zonePosManager.spawnZone(level, chunkPos);
        }
    }


}
