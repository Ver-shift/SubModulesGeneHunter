package org.galaxy.beyond.api.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.spawn.character.Character;

import java.util.Locale;

public final class BeyondRegistries {

    private BeyondRegistries() {
    }

    // ========== Registry 实例 ==========

    public static final Registry<RogueCap> ROGUE_CAP = new RegistryBuilder<>(Keys.ROGUE_CAP).create();
    public static final Registry<RogueEventType> ROGUE_EVENT_TYPE = new RegistryBuilder<>(Keys.ROGUE_EVENT_TYPE).create();
    public static final Registry<Character> SPAWN_CHARACTER = new RegistryBuilder<>(Keys.SPAWN_CHARACTER).create();

    // ========== DeferredRegister ==========

    public static final DeferredRegister<RogueCap> ROGUE_CAP_REGISTER =
            DeferredRegister.create(Keys.ROGUE_CAP, Beyond.MODID);
    public static final DeferredRegister<RogueEventType> ROGUE_EVENT_TYPE_REGISTER =
            DeferredRegister.create(Keys.ROGUE_EVENT_TYPE, Beyond.MODID);
    public static final DeferredRegister<Character> SPAWN_CHARACTER_REGISTER =
            DeferredRegister.create(Keys.SPAWN_CHARACTER, Beyond.MODID);

    // ========== 生命周期 ==========

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(ROGUE_CAP);
        event.register(ROGUE_EVENT_TYPE);
        event.register(SPAWN_CHARACTER);
    }

    public static void register(IEventBus eventBus) {
        ROGUE_CAP_REGISTER.register(eventBus);
        ROGUE_EVENT_TYPE_REGISTER.register(eventBus);
        SPAWN_CHARACTER_REGISTER.register(eventBus);
    }

    // ========== Keys ==========

    public static final class Keys {
        private Keys() {
        }

        public static final String REGISTRY_NAMESPACE = Beyond.MODID;

        public static final ResourceKey<Registry<RogueCap>> ROGUE_CAP =
                ResourceKey.createRegistryKey(namedRegistry("rogue_cap"));
        public static final ResourceKey<Registry<RogueEventType>> ROGUE_EVENT_TYPE =
                ResourceKey.createRegistryKey(namedRegistry("rogue_event_type"));
        public static final ResourceKey<Registry<Character>> SPAWN_CHARACTER =
                ResourceKey.createRegistryKey(namedRegistry("spawn_character"));

        private static ResourceLocation namedRegistry(String name) {
            return ResourceLocation.parse(REGISTRY_NAMESPACE + ":" + name.toLowerCase(Locale.ROOT));
        }
    }
}
