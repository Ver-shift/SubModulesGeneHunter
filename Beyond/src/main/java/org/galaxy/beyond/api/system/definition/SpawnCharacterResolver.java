package org.galaxy.beyond.api.system.definition;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondRegistries;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;
import org.galaxy.beyond.api.system.spawn.character.Character;

public final class SpawnCharacterResolver {

    private SpawnCharacterResolver() {
    }

    public static Character resolve(SpawnDefinition definition) {
        ResourceLocation id = definition.getCharacter();
        if (id == null) {
            id = CassandraCharacter.ID;
        }
        Character character = BeyondRegistries.SPAWN_CHARACTER.get(id);
        if (character != null) {
            return character;
        }
        return BeyondRegistries.SPAWN_CHARACTER.get(Beyond.asResource("cassandra"));
    }
}
