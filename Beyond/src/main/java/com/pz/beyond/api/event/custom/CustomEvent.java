package com.pz.beyond.api.event.custom;

import com.pz.beyond.api.BeyondAPI;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class CustomEvent {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var playerData = BeyondAPI.getBeyondPlayerData(serverPlayer);
        var playerLoginData = playerData.getPlayerLoginData();
        if (!playerLoginData.isFirstLogin()) {
            return;
        }
        NeoForge.EVENT_BUS.post(new PlayerFirstLoggedInEvent(serverPlayer));
        playerLoginData.setFirstLogin(false);
    }
}
