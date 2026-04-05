package org.biotech.api.event.handle;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.biotech.Biotech;
import org.biotech.api.pack.LootPack;

///**
// * 基因战利品表事件处理器
// * 负责注册数据包重载监听器
// */
@EventBusSubscriber(modid = Biotech.MODID)
public class GeneLootTableEventHandler {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            LootPack.syncToPlayer(event.getPlayer(), LootPack.getLatestTables());
            return;
        }

        LootPack.syncToAllPlayers(LootPack.getLatestTables());
        Biotech.LOGGER.debug("Synced gene loot tables to all online players");
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            LootPack.syncToPlayer(player, LootPack.getLatestTables());
        }
    }
}
