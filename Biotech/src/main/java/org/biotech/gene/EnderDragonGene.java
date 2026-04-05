package org.biotech.gene;

import net.minecraft.resources.ResourceLocation;
import org.biotech.Biotech;
import org.biotech.api.system.gene.GeneConfigBuilder;
import org.biotech.api.system.gene.core.GeneRarity;
import org.biotech.api.system.gene.core.IGene;
import org.biotech.api.util.AutoInit;


@AutoInit(type = AutoInit.InitType.GENE)
public class EnderDragonGene implements IGene {
    @Override
    public ResourceLocation getID() {
        return Biotech.asResource("ender_dragon_gene");
    }

    @Override
    public GeneConfigBuilder getConfigBuilder() {
        return GeneConfigBuilder
                .builder()
                .rarity(GeneRarity.EPIC);
    }
}
