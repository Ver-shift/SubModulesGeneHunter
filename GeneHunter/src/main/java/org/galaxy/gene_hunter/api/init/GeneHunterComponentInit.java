package org.galaxy.gene_hunter.api.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent;

import java.util.function.Supplier;

public class GeneHunterComponentInit {

    public static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, GeneHunter.MODID);

    public static final Supplier<DataComponentType<WeaponClassComponent>> WEAPON_CLASS =
            REGISTRAR.registerComponentType("weapon_class", builder -> builder
                    .persistent(WeaponClassComponent.CODEC)
                    .networkSynchronized(WeaponClassComponent.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }
}
