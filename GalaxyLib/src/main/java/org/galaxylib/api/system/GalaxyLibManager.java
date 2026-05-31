package org.galaxylib.api.system;

import lombok.Getter;
import org.galaxylib.api.system.loot.LootManager;

public class GalaxyLibManager {

    @Getter
    private final LootManager lootManager = new LootManager();
}
