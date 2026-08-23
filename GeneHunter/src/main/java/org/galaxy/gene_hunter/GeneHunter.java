package org.galaxy.gene_hunter;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterComponentInit;
import org.galaxy.gene_hunter.api.init.GeneHunterCreativeTabInit;
import org.galaxy.gene_hunter.api.init.GeneHunterEntityInit;
import org.galaxy.gene_hunter.api.init.GeneHunterGatewayInit;
import org.galaxy.gene_hunter.api.init.GeneHunterItemInit;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxy.gene_hunter.api.init.GeneHunterMenuInit;
import org.galaxy.gene_hunter.api.init.GeneHunterPacketInit;
import org.galaxy.gene_hunter.api.init.GeneHunterRogueEventInit;
import org.galaxy.gene_hunter.api.init.GeneHunterSpawnInit;
import org.galaxy.gene_hunter.event.GeneHunterCommonEvents;

@Mod(GeneHunter.MODID)
public class GeneHunter {
    public static final String MODID = "gene_hunter";

    public GeneHunter(IEventBus modEventBus, ModContainer modContainer) {

        GeneHunterAttachInit.register(modEventBus);
        GeneHunterComponentInit.register(modEventBus);
        GeneHunterItemInit.register(modEventBus);
        GeneHunterCreativeTabInit.register(modEventBus);
        GeneHunterEntityInit.register(modEventBus);
        GeneHunterLootInit.register(modEventBus);
        GeneHunterRogueEventInit.register(modEventBus);
        GeneHunterAttributeInit.register(modEventBus);
        GeneHunterMenuInit.register();
        GeneHunterGatewayInit.register(modEventBus);
        GeneHunterSpawnInit.register(modEventBus);
        GeneHunterCommonEvents.register(modEventBus);
        modEventBus.addListener(GeneHunterPacketInit::register);

    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase());
    }

}

