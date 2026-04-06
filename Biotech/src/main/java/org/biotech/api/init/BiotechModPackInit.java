package org.biotech.api.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.biotech.Biotech;
import org.galaxylib.api.system.loot.LootPack;


@EventBusSubscriber(modid = Biotech.MODID)
public class BiotechModPackInit {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {

    }
}
