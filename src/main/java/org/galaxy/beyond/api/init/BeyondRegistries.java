package org.galaxy.beyond.api.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.zone.ZoneCapType;

import java.util.Locale;

public final class BeyondRegistries {

    private BeyondRegistries() {}

    // ========== Registry 实例 ==========

    public static final Registry<ZoneCapType> ZONE_CAP_TYPE = new RegistryBuilder<>(Keys.ZONE_CAP_TYPE).create();
    public static final Registry<PlayerPhase> PLAYER_PHASE = new RegistryBuilder<>(Keys.PLAYER_PHASE).create();
    public static final Registry<NodePhase> NODE_PHASE = new RegistryBuilder<>(Keys.NODE_PHASE).create();
    public static final Registry<RoguePhase> ROGUE_PHASE = new RegistryBuilder<>(Keys.ROGUE_PHASE).create();

    // ========== DeferredRegister ==========

    public static final DeferredRegister<ZoneCapType> ZONE_CAP_TYPE_REGISTER =
            DeferredRegister.create(Keys.ZONE_CAP_TYPE, Beyond.MODID);
    public static final DeferredRegister<PlayerPhase> PLAYER_PHASE_REGISTER =
            DeferredRegister.create(Keys.PLAYER_PHASE, Beyond.MODID);
    public static final DeferredRegister<NodePhase> NODE_PHASE_REGISTER =
            DeferredRegister.create(Keys.NODE_PHASE, Beyond.MODID);
    public static final DeferredRegister<RoguePhase> ROGUE_PHASE_REGISTER =
            DeferredRegister.create(Keys.ROGUE_PHASE, Beyond.MODID);

    // ========== 生命周期 ==========

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(ZONE_CAP_TYPE);
        event.register(PLAYER_PHASE);
        event.register(NODE_PHASE);
        event.register(ROGUE_PHASE);
    }

    public static void register(IEventBus eventBus) {
        ZONE_CAP_TYPE_REGISTER.register(eventBus);
        PLAYER_PHASE_REGISTER.register(eventBus);
        NODE_PHASE_REGISTER.register(eventBus);
        ROGUE_PHASE_REGISTER.register(eventBus);
    }

    // ========== Keys ==========

    public static final class Keys {
        private Keys() {}

        public static final String REGISTRY_NAMESPACE = Beyond.MODID;

        public static final ResourceKey<Registry<ZoneCapType>> ZONE_CAP_TYPE =
                ResourceKey.createRegistryKey(namedRegistry("zone_cap_type"));
        public static final ResourceKey<Registry<PlayerPhase>> PLAYER_PHASE =
                ResourceKey.createRegistryKey(namedRegistry("player_phase"));
        public static final ResourceKey<Registry<NodePhase>> NODE_PHASE =
                ResourceKey.createRegistryKey(namedRegistry("node_phase"));
        public static final ResourceKey<Registry<RoguePhase>> ROGUE_PHASE =
                ResourceKey.createRegistryKey(namedRegistry("rogue_phase"));

        private static Identifier namedRegistry(String name) {
            return Identifier.parse(REGISTRY_NAMESPACE + ":" + name.toLowerCase(Locale.ROOT));
        }
    }
}
