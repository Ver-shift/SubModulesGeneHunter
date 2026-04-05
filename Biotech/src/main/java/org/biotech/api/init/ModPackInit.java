package org.biotech.api.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.biotech.Biotech;
import org.biotech.api.pack.LootPack;


@EventBusSubscriber(modid = Biotech.MODID)
public class ModPackInit {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new LootPack());
        Biotech.LOGGER.debug("Registered GeneLootTable data pack listener");

    }
}
