package org.biotech.api.system.gene;

import org.biotech.api.system.gene.core.IGene;

import java.util.function.Supplier;

/**
 * 给武器或者生物单独添加基因。
 */
public class GeneRegistryHolder {
    Supplier<IGene> registryGene;

    public GeneRegistryHolder(Supplier<IGene> registryGene){
        this.registryGene = registryGene;
    }

    public GeneInstance getInstance(){
        return new GeneInstance(registryGene.get());
    }

    public static GeneRegistryHolder[] of(GeneRegistryHolder... args) {
        return args;
    }
}
