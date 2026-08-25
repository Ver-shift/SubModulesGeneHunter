package org.biotech.api.init;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.biotech.Biotech;
import org.biotech.api.system.gene.GeneDefinition;

import java.util.List;
import java.util.Optional;

public final class BiotechGeneInit {
    public static final ResourceKey<Registry<GeneDefinition>> GENE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Biotech.asResource("gene_registry"));
    public static final ResourceKey<Registry<GeneDefinition>> XENE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Biotech.asResource("xene_registry"));
    private BiotechGeneInit() {}

    public static void register(IEventBus eventBus) {
        eventBus.addListener(BiotechGeneInit::registerRegistry);
    }

    /** Registers a synced registry loaded exclusively from datapacks. */
    public static void registerRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(GENE_REGISTRY_KEY, GeneDefinition.CODEC, GeneDefinition.CODEC);
        event.dataPackRegistry(XENE_REGISTRY_KEY, GeneDefinition.CODEC, GeneDefinition.CODEC);
    }

    public static Optional<GeneDefinition> getGene(RegistryAccess registryAccess, ResourceLocation id) {
        return registryAccess.registry(GENE_REGISTRY_KEY).flatMap(registry -> Optional.ofNullable(registry.get(id)));
    }

    public static Optional<GeneDefinition> getXene(RegistryAccess registryAccess, ResourceLocation id) {
        return registryAccess.registry(XENE_REGISTRY_KEY).flatMap(registry -> Optional.ofNullable(registry.get(id)));
    }

    public static List<ResourceLocation> getGeneIds(RegistryAccess registryAccess) {
        return getIds(registryAccess, GENE_REGISTRY_KEY);
    }

    public static List<ResourceLocation> getXeneIds(RegistryAccess registryAccess) {
        return getIds(registryAccess, XENE_REGISTRY_KEY);
    }

    private static List<ResourceLocation> getIds(RegistryAccess registryAccess, ResourceKey<Registry<GeneDefinition>> key) {
        return registryAccess.registry(key)
                .map(registry -> registry.keySet().stream().toList())
                .orElseGet(List::of);
    }
}
