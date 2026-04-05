package org.galaxy.genehunter;

import com.pz.beyond.Beyond;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.galaxy.biotech.Biotech;
import org.galaxy.gene_hunter.Common;

@Mod(GeneHunter.MODID)
public class GeneHunter {
    public static final String MODID = "gene_hunter";

    public GeneHunter(IEventBus modEventBus, ModContainer modContainer) {
        Common.gogogog();
        var biotech_id = Biotech.MODID;
        var beyond_id = Beyond.MODID;
    }
}

