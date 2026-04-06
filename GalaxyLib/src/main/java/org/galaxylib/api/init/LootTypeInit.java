package org.biotech.api.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.biotech.Biotech;
import org.biotech.api.system.loot.core.ILootType;
import org.biotech.loot.*;

import java.util.function.Supplier;

/**
 * 战利品类型注册中心
 */
public class  LootTypeInit {
    
    public static final ResourceKey<Registry<ILootType<?>>> LOOT_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Biotech.asResource("loot_type_registry"));
    
    public static final Registry<ILootType<?>> LOOT_TYPE_REGISTRY = new RegistryBuilder<>(LOOT_TYPE_REGISTRY_KEY).create();
    
    public static final DeferredRegister<ILootType<?>> LOOT_TYPE =
            DeferredRegister.create(LOOT_TYPE_REGISTRY_KEY, Biotech.MODID);

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

    
    /**
     * 根据名称获取战利品类型
     */
    public static ILootType<?> getLootTypeByName(String name) {
        return LOOT_TYPE_REGISTRY.stream()
                .filter(type -> type.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * 注册战利品类型
     */
    public static <T extends ILootType<?>> Supplier<ILootType<?>> registerLootType(Supplier<T> supplier) {
        return LOOT_TYPE.register(supplier.get().getName(), supplier);
    }

    // ==================== 战利品类型注册 ====================

    public static final Supplier<ILootType<?>> FOOD_LOOT_TYPE;
    public static final Supplier<ILootType<?>> XENE_TRAIT_LOOT_TYPE;
    public static final Supplier<ILootType<?>> GENE_TRAIT_LOOT_TYPE;
    public static final Supplier<ILootType<?>> WEAPON_LOOT_TYPE;
    public static final Supplier<ILootType<?>> GENE_LOOT_TYPE;

    static {
        FOOD_LOOT_TYPE = registerLootType(FoodLootType::new);
        XENE_TRAIT_LOOT_TYPE = registerLootType(XeneTraitLootType::new);
        GENE_TRAIT_LOOT_TYPE = registerLootType(GeneTraitLootType::new);
        WEAPON_LOOT_TYPE = registerLootType(WeaponLootType::new);
        GENE_LOOT_TYPE = registerLootType(GeneLootType::new);
    }
}
