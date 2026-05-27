package org.galaxy.gene_hunter;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxy.gene_hunter.api.init.GeneHunterMenuInit;

@Mod(GeneHunter.MODID)
public class GeneHunter {
    public static final String MODID = "gene_hunter";

    public GeneHunter(IEventBus modEventBus, ModContainer modContainer) {

        GeneHunterAttachInit.register(modEventBus);
        GeneHunterLootInit.register(modEventBus);
        GeneHunterAttributeInit.register(modEventBus);
        GeneHunterMenuInit.register();

    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID,path.toLowerCase());
    }

}

