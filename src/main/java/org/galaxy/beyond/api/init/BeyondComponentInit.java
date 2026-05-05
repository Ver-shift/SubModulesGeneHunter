package org.galaxy.beyond.api.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.component.ValueComp;

import java.util.function.Supplier;

public class BeyondComponentInit {
    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Beyond.MODID);


    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }

    public static final Supplier<DataComponentType<ValueComp>> ITEM_VALUE = REGISTRAR
            .registerComponentType("item_value",
                    valueCompBuilder -> valueCompBuilder
                            .persistent(ValueComp.CODEC)
                            .networkSynchronized(ValueComp.STREAM_CODEC));

}
