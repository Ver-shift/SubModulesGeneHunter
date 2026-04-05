package org.galaxy.biotech;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.galaxy.gene_hunter.Common;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Biotech.MODID)
public class Biotech {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "biotech";

    public Biotech(IEventBus modEventBus, ModContainer modContainer) {
        System.out.println(new Common());
        Common.gogogog();
    }


}
