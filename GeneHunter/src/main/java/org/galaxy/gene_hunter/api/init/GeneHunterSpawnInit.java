package org.galaxy.gene_hunter.api.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.api.init.BeyondRegistries;
import org.galaxy.beyond.api.system.spawn.character.Character;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.character.GeneHunterSpawnCharacter;

import java.util.function.Supplier;

public final class GeneHunterSpawnInit {

    private static final DeferredRegister<Character> REGISTER =
            DeferredRegister.create(BeyondRegistries.Keys.SPAWN_CHARACTER, GeneHunter.MODID);

    public static final Supplier<Character> GENE_HUNTER_SPAWN =
            REGISTER.register("gene_hunter_spawn", GeneHunterSpawnCharacter::new);

    private GeneHunterSpawnInit() {
    }

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
