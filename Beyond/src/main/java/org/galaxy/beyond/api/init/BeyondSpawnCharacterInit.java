package org.galaxy.beyond.api.init;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;
import org.galaxy.beyond.api.system.spawn.character.Character;
import org.galaxy.beyond.api.system.spawn.character.PhoebeCharacter;
import org.galaxy.beyond.api.system.spawn.character.RandyCharacter;

import java.util.function.Supplier;

public final class BeyondSpawnCharacterInit {

    public static final Supplier<Character> CASSANDRA = register(CassandraCharacter.ID, CassandraCharacter::new);
    public static final Supplier<Character> RANDY = register(RandyCharacter.ID, RandyCharacter::new);
    public static final Supplier<Character> PHOEBE = register(PhoebeCharacter.ID, PhoebeCharacter::new);

    private BeyondSpawnCharacterInit() {
    }

    public static void register(IEventBus eventBus) {
    }

    private static Supplier<Character> register(ResourceLocation id, Supplier<? extends Character> supplier) {
        return BeyondRegistries.SPAWN_CHARACTER_REGISTER.register(id.getPath(), supplier);
    }
}
