package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.progress.encounter.BossEncounter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

/**
 * 遭遇类型注册
 */
public class BeyondEncounters {

    public static final ResourceKey<Registry<EncounterType>> ENCOUNTER_REGISTRY_KEY =
        ResourceKey.createRegistryKey(Beyond.asResource("encounters"));

    public static final Registry<EncounterType> ENCOUNTER_REGISTRY = new RegistryBuilder<>(ENCOUNTER_REGISTRY_KEY).create();

    public static final DeferredRegister<EncounterType> ENCOUNTERS =
        DeferredRegister.create(ENCOUNTER_REGISTRY_KEY, Beyond.MODID);

    // Boss 遭遇（红色节点 + 高潮场景）
    public static final Supplier<EncounterType> BOSS_RED = ENCOUNTERS.register("boss",
        () -> new BossEncounter(BeyondNodeColors.RED));
    public static final Supplier<EncounterType> BOSS_GREEN = ENCOUNTERS.register("harvest",
        () -> new BossEncounter(BeyondNodeColors.GREEN));
    public static final Supplier<EncounterType> BOSS_ORANGE = ENCOUNTERS.register("repose",
        () -> new BossEncounter(BeyondNodeColors.ORANGE));


    public static void registerRegistry(NewRegistryEvent event) {
        event.register(ENCOUNTER_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        ENCOUNTERS.register(eventBus);
    }

    public static EncounterType getById(ResourceLocation id) {
        return ENCOUNTER_REGISTRY.get(id);
    }
}
