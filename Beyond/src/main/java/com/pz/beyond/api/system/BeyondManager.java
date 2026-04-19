package com.pz.beyond.api.system;

import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.structure.PlayerStructureManager;
import com.pz.beyond.api.system.structure.StructureManager;
import com.pz.beyond.api.system.zone.ZoneManager;
import lombok.NoArgsConstructor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Level-scoped runtime manager that dispatches zone rules.
 */
@NoArgsConstructor
public class BeyondManager implements IBeyondManager {

    private final ZoneManager zoneManager = new ZoneManager();
    private final StructureManager structureManager = new StructureManager();

    private final PlayerStructureManager structureManagerPlayer = new PlayerStructureManager();

    @Override
    public void levelTick(Level level) {
        if (level instanceof ServerLevel serverLevel && BeyondAttachInit.isAllowedDimension(serverLevel)) {
            VillageStructureScanService.tick(serverLevel);
            zoneManager.handleZoneRule(serverLevel);
        }
    }

    @Override   
    public void loadLevel(ServerLevel serverLevel) {
        structureManager.findSafeZone(serverLevel);
    }

    @Override
    public void playerFirstLoad(ServerPlayer serverPlayer) {
        structureManagerPlayer.handlePlayerFirstLoad(serverPlayer);
    }


}
