package org.galaxy.beyond.api.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.rogue_event.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BeyondRogueEventTypeInit {

    public static final ResourceKey<Registry<RogueEventType>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.parse(Beyond.MODID + ":rogue_event_type"));

    public static final Registry<RogueEventType> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    public static final DeferredRegister<RogueEventType> REGISTER =
            DeferredRegister.create(REGISTRY_KEY, Beyond.MODID);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static Optional<Holder.Reference<RogueEventType>> getById(Identifier id) {
        return REGISTRY.get(id);
    }

    public static List<RogueEventType> getAll() {
        return REGISTRY.stream().collect(Collectors.toList());
    }

    public static Supplier<RogueEventType> registerEvent(Supplier<? extends RogueEventType> sup) {
        return REGISTER.register(sup.get().getId().getPath(), sup);
    }

    public static final Supplier<RogueEventType> BOSS = registerEvent(BossEvent::new);
    public static final Supplier<RogueEventType> HEAL = registerEvent(HealEvent::new);
    public static final Supplier<RogueEventType> MONSTER = registerEvent(MonsterEvent::new);
    public static final Supplier<RogueEventType> SHOP = registerEvent(ShopEvent::new);
    public static final Supplier<RogueEventType> REWARD = registerEvent(RewardEvent::new);
}
