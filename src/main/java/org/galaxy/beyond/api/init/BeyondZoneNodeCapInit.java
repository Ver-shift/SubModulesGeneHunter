package org.galaxy.beyond.api.init;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.zone_cap.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BeyondZoneNodeCapInit {

    public static final ResourceKey<Registry<ZoneCapType>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.parse(Beyond.MODID + ":zone_cap_type"));

    public static final Registry<ZoneCapType> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    public static final DeferredRegister<ZoneCapType> REGISTER =
            DeferredRegister.create(REGISTRY_KEY, Beyond.MODID);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static Optional<Holder.Reference<ZoneCapType>> getById(Identifier id) {
        return REGISTRY.get(id);
    }

    public static List<ZoneCapType> getAll() {
        return REGISTRY.stream().collect(Collectors.toList());
    }

    public static Supplier<ZoneCapType> registerCap(Supplier<? extends ZoneCapType> sup) {
        return REGISTER.register(sup.get().getId().getPath(), sup);
    }

    public static final Supplier<ZoneCapType> ALL_NODE_ZONE = registerCap(AllNodeZoneCap::new);
    public static final Supplier<ZoneCapType> ALL_SAFE_ZONE = registerCap(AllSafeZoneCap::new);
}
