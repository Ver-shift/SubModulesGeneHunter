package org.galaxy.gene_hunter.api.init;

import org.galaxy.beyond.api.system.definition.SpawnDefinitionManager;
import org.galaxy.gene_hunter.rogue_event.ZombieGatewayCharacter;

public final class GeneHunterSpawnInit {

    private GeneHunterSpawnInit() {
    }

    public static void register() {
        SpawnDefinitionManager.registerCharacter(new ZombieGatewayCharacter());
    }
}
