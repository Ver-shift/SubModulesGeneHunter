package org.biotech.api.event.handle;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.biotech.api.GeneData;
import org.biotech.api.init.AttachInit;

@EventBusSubscriber
public class GeneDataHandle {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneData geneData = serverPlayer.getData(AttachInit.GENE_DATA);
            geneData.setPlayer(serverPlayer);
            geneData.setPlayerId(serverPlayer.getId());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneData geneData = serverPlayer.getData(AttachInit.GENE_DATA);
            geneData.setPlayer(serverPlayer);
            geneData.setPlayerId(serverPlayer.getId());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneData geneData = serverPlayer.getData(AttachInit.GENE_DATA);
            geneData.setPlayer(serverPlayer);
            geneData.setPlayerId(serverPlayer.getId());
        }
    }
}
