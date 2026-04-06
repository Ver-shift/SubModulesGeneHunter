package org.biotech.api.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.component.Example;
import org.biotech.component.TraitComp;

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



    public static final Supplier<DataComponentType<TraitComp>> TRAIT_COMP = REGISTRAR.registerComponentType(
            "trait_comp",
            traitCompBuilder -> traitCompBuilder
                    .persistent(TraitComp.CODEC)
                    .networkSynchronized(TraitComp.STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<GeneInstance>> GENE_INSTANCE = REGISTRAR.registerComponentType(
            "gene_instance",
            geneInstanceBuilder -> geneInstanceBuilder
                    .persistent(GeneInstance.CODEC)
                    .networkSynchronized(GeneInstance.STREAM_CODEC)
    );

}
