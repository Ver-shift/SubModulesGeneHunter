package com.pz.beyond;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.galaxy.gene_hunter.Common;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Beyond.MODID)
public class Beyond {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "beyond";

    public Beyond(IEventBus modEventBus, ModContainer modContainer) {
        System.out.println(new Common());
    }



}
