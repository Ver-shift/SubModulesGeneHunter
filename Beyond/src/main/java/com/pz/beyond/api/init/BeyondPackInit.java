package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.pack.ProgressPack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondPackInit {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ProgressPack());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ProgressPack.applyToServer(event.getPlayerList().getServer());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        var server = event.getEntity().getServer();
        if (server != null) {
            ProgressPack.applyToServer(server);
        }
    }
}
