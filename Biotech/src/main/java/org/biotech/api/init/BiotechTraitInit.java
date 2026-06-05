package org.biotech.api.init;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.biotech.Biotech;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.util.ModPluginFinder;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BiotechTraitInit {
    public static final ResourceKey<Registry<ITrait>> TRAIT_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Biotech.asResource("trait_registry"));
    public static final Registry<ITrait> TRAIT_REGISTRY = new RegistryBuilder<>(TRAIT_REGISTRY_KEY).create();
    public static final DeferredRegister<ITrait> TRAIT =
            DeferredRegister.create(TRAIT_REGISTRY_KEY, Biotech.MODID);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(TRAIT_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        TRAIT.register(eventBus);
    }
    public static final ITrait EMPTY = new ITrait() {
        @Override
        public ResourceLocation getId() {
            return ITrait.EMPTY_TRAIT_ID;
        }

        @Override
        public List<MutableComponent> getUniqueInfo() {
            return List.of();
        }
    };

    public static ITrait getTraitById(ResourceLocation traitId) {
        var trait = TRAIT_REGISTRY.get(traitId);
        if (trait == null) {
            Biotech.LOGGER.warn("BiotechTraitInit.getTraitById: Trait not found for id: {}, registry size: {}",
                traitId, TRAIT_REGISTRY.keySet().size());
            return EMPTY;
        }
        return trait;
    }
    

    
    /**
     * 获取词条的 ResourceLocation ID
     * 用于组件序列化
     */
    public static ResourceLocation getTraitId(ITrait trait) {
        return trait != null ? trait.getId() : null;
    }

    public static List<ITrait> getAllTraits() {
        return TRAIT_REGISTRY.stream().collect(Collectors.toList());
    }

    public static Supplier<ITrait> registerTrait(Supplier<? extends ITrait> sup) {
        return TRAIT.register(sup.get().getId().getPath(), sup);
    }

    /**
     * 通过 @AutoInit(type = TRAIT) 注解自动发现并注册词条
     * 扫描所有带有该注解的 ITrait 实现类并注册
     */
    public static void autoRegisterTraits() {
        // 扫描 @AutoInit(type = TRAIT) 注解的类
        List<ITrait> autoTraits = ModPluginFinder.getModTraits();
        if (autoTraits.isEmpty()) {
            Biotech.LOGGER.warn("No traits found for auto-registration!");
        }
        for (ITrait trait : autoTraits) {
            String path = trait.getId().getPath();
            TRAIT.register(path, () -> trait);
            Biotech.LOGGER.info("Auto-registered trait: {}", trait.getId());
        }
    }



    static{

    }


}
