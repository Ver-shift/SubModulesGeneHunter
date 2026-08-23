package org.biotech.api.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.loot.GeneLootType;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class BiotechLootTypeInit {

    // Use the same shared loot type registry id without triggering GalaxyLib class loading in clinit.


    private static final DeferredRegister<ILootType<?>> REGISTRAR = DeferredRegister.create(GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY_KEY, Biotech.MODID);

     public static void register(IEventBus eventBus) {
         REGISTRAR.register(eventBus);
     }

    /**
     * 注册战利品类型
     */
    public static <T extends ILootType<?>> Supplier<ILootType<?>> registerLootType(String name, Supplier<T> supplier) {
        return REGISTRAR.register(name, supplier);
    }
    public static final Supplier<ILootType<?>> GENE_LOOT_TYPE;

    static {
        GENE_LOOT_TYPE = registerLootType("gene", GeneLootType::new);
    }
}
