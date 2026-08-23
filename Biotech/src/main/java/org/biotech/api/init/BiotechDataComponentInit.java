package org.biotech.api.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.component.Example;

import java.util.function.Supplier;

public class BiotechDataComponentInit {

    public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }


    public static final Supplier<DataComponentType<Example>> EXAMPLE = REGISTRAR.registerComponentType(
        "example",
            exampleBuilder -> exampleBuilder
                    .persistent(Example.CODEC)
                    .networkSynchronized(Example.STREAM_CODEC)
    );



    public static final Supplier<DataComponentType<GeneInstance>> GENE_INSTANCE = REGISTRAR.registerComponentType(
            "gene_instance",
            geneInstanceBuilder -> geneInstanceBuilder
                    .persistent(GeneInstance.CODEC)
                    .networkSynchronized(GeneInstance.STREAM_CODEC)
    );

    /** Snapshot of the datapack description, retained on the stack for client tooltips. */
    public static final Supplier<DataComponentType<Component>> GENE_DESCRIPTION = REGISTRAR.registerComponentType(
            "gene_description",
            builder -> builder
                    .persistent(ComponentSerialization.CODEC)
                    .networkSynchronized(ComponentSerialization.STREAM_CODEC)
    );

}
