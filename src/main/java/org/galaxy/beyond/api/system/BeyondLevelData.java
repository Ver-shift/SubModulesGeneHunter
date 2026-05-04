package org.galaxy.beyond.api.system;

import lombok.Data;
import lombok.NonNull;
import org.galaxy.beyond.api.system.rogue.RogueData;
import org.galaxy.beyond.api.system.structure.SafeZoneStructureData;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

@Data
public class BeyondLevelData {

    private final RogueData rogueData = new RogueData();
    private final LevelZoneData levelZoneData = new LevelZoneData();

    private final SafeZoneStructureData safeZoneStructureData = new SafeZoneStructureData();
}
