package org.galaxy.beyond.api.system.definition;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.init.BeyondRegistries;
import org.galaxy.beyond.api.system.spawn.character.Character;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SpawnDefinitionManager {

    private static final Map<ResourceLocation, SpawnDefinition> DEFINITIONS = new HashMap<>();

    static {
        registerDefinition(SpawnDefinition.builder()
                .id(CassandraCharacter.ID)
                .character(CassandraCharacter.ID)
                .build());
    }

    private SpawnDefinitionManager() {
    }

    public static void setDefinitions(Map<ResourceLocation, SpawnDefinition> definitions) {
        DEFINITIONS.clear();
        registerDefinition(SpawnDefinition.builder()
                .id(CassandraCharacter.ID)
                .character(CassandraCharacter.ID)
                .build());
        if (definitions != null) {
            DEFINITIONS.putAll(definitions);
        }
    }

    public static Map<ResourceLocation, SpawnDefinition> definitions() {
        return Map.copyOf(DEFINITIONS);
    }

    public static Optional<SpawnDefinition> getDefinition(ResourceLocation id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static SpawnDefinition getDefinitionOrThrow(ResourceLocation id) {
        return getDefinition(id).orElseThrow(() -> new IllegalStateException("Missing spawn definition: " + id));
    }

    public static void registerDefinition(SpawnDefinition definition) {
        DEFINITIONS.put(definition.getId(), definition);
    }

    public static Character getCharacter(SpawnDefinition definition) {
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
