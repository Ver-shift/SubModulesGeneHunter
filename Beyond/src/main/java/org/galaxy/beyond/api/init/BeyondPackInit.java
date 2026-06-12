package org.galaxy.beyond.api.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.pack.ProgressDataPack;
import org.galaxy.beyond.api.pack.SpawnDataPack;

@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondPackInit {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ProgressDataPack());
        event.addListener(new SpawnDataPack());
    }
}
