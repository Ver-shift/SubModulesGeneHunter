package org.galaxy.beyond.data.spawn;

import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;

public class CassandraSpawnDefinition {

    public SpawnDefinition build() {
        return SpawnDefinition.builder()
                .id(CassandraCharacter.ID)
                .character(CassandraCharacter.ID)
                .build();
    }
}
