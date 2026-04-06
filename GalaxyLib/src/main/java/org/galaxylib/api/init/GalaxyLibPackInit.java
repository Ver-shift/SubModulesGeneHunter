package org.galaxylib.api.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxylib.GalaxyLib;
import org.galaxylib.api.system.loot.LootPack;

@EventBusSubscriber(modid = GalaxyLib.MODID)
public class GalaxyLibPackInit {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new LootPack());
    }


    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            LootPack.syncToPlayer(event.getPlayer(), LootPack.getLatestTables());
            return;
        }

        LootPack.syncToAllPlayers(LootPack.getLatestTables());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            LootPack.syncToPlayer(player, LootPack.getLatestTables());
        }
    }

}
