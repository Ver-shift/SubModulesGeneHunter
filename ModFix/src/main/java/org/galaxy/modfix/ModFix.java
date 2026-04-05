package org.galaxy.modfix;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.galaxy.gene_hunter.Common;

@Mod(ModFix.MODID)
public class ModFix {
    public static final String MODID = "modfix";

    public ModFix(IEventBus modEventBus, ModContainer modContainer) {
        System.out.println(new Common());
    }
}

