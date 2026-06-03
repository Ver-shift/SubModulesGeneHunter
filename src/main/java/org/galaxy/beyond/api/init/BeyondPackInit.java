package org.galaxy.beyond.api.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.pack.ProgressDataPack;

@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondPackInit {

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Beyond.asResource("progress_data"), new ProgressDataPack());
    }
}
