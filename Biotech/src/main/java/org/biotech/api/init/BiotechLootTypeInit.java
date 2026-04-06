package org.biotech.api.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.loot.GeneLootType;
import org.biotech.loot.GeneTraitLootType;
import org.biotech.loot.XeneTraitLootType;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.function.Supplier;

import static org.galaxylib.api.init.GalaxyLibLootTypeInit.registerLootType;

public class BiotechLootTypeInit {

    private static final DeferredRegister<ILootType<?>> REGISTRAR = DeferredRegister.create(GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY_KEY, Biotech.MODID);

     public static void register(IEventBus eventBus) {
         REGISTRAR.register(eventBus);
     }

    public static final Supplier<ILootType<?>> XENE_TRAIT_LOOT_TYPE;
    public static final Supplier<ILootType<?>> GENE_TRAIT_LOOT_TYPE;
    public static final Supplier<ILootType<?>> GENE_LOOT_TYPE;

    static {
        XENE_TRAIT_LOOT_TYPE = registerLootType(XeneTraitLootType::new);
        GENE_TRAIT_LOOT_TYPE = registerLootType(GeneTraitLootType::new);
        GENE_LOOT_TYPE = registerLootType(GeneLootType::new);
    }
}
