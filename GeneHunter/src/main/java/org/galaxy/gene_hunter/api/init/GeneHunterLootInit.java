package org.galaxy.gene_hunter.api.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.loot.FoodLootType;
import org.galaxy.gene_hunter.loot.WeaponLootType;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.function.Supplier;

public class GeneHunterLootInit {

    private static final DeferredRegister<ILootType<?>> REGISTRAR = DeferredRegister.create(GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY_KEY, GeneHunter.MODID);

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }

    /**
     * 注册战利品类型
     */
    public static <T extends ILootType<?>> Supplier<ILootType<?>> registerLootType(Supplier<T> supplier) {
        return REGISTRAR.register(supplier.get().getName(), supplier);
    }

    public static final Supplier<ILootType<?>> FOOD_LOOT_TYPE;
    public static final Supplier<ILootType<?>> WEAPON_LOOT_TYPE;


    static {
        FOOD_LOOT_TYPE = registerLootType(FoodLootType::new);
        WEAPON_LOOT_TYPE = registerLootType(WeaponLootType::new);
    }
}
