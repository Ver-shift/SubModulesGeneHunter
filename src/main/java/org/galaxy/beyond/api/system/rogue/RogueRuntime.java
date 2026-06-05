package org.galaxy.beyond.api.system.rogue;

import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;

public final class RogueRuntime {

    private RogueRuntime() {
    }

    public static boolean isActive(Level level) {
        if (!CommonConfig.isRogueDimension(level)) return false;
        if (BeyondAPI.getSafeZoneStructureData(level).getInitialized() < 1) return false;
        return BeyondAPI.getLevelZoneData(level).hasZones();
    }
}
