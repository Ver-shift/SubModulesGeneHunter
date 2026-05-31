package org.galaxylib.api.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.galaxylib.GalaxyLib;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.function.Supplier;

/**
 * 战利品类型注册中心
 */
public class GalaxyLibLootTypeInit {
    
    public static final ResourceKey<Registry<ILootType<?>>> LOOT_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(GalaxyLib.MODID,"loot_type_registry"));
    
    public static final Registry<ILootType<?>> LOOT_TYPE_REGISTRY = new RegistryBuilder<>(LOOT_TYPE_REGISTRY_KEY).create();
    
    public static final DeferredRegister<ILootType<?>> LOOT_TYPE =
            DeferredRegister.create(LOOT_TYPE_REGISTRY_KEY, GalaxyLib.MODID);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(LOOT_TYPE_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        LOOT_TYPE.register(eventBus);
    }

    /**
     * 根据 ID 获取战利品类型
     */
    public static ILootType<?> getLootTypeById(ResourceLocation lootTypeId) {
        return LOOT_TYPE_REGISTRY.get(lootTypeId);
    }

    public static ResourceLocation getLootTypeId(ILootType<?> lootType) {
        return LOOT_TYPE_REGISTRY.getKey(lootType);
    }

    
    /**
     * 根据名称获取战利品类型
     */
    /**
     * 注册战利品类型
     */
    public static <T extends ILootType<?>> Supplier<ILootType<?>> registerLootType(String name, Supplier<T> supplier) {
        return LOOT_TYPE.register(name, supplier);
    }

    // ==================== 战利品类型注册 ====================

}
