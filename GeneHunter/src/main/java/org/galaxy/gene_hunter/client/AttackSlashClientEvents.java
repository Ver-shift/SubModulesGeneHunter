package org.galaxy.gene_hunter.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.network.PlayerSwingPayload;

@EventBusSubscriber(modid = GeneHunter.MODID, value = Dist.CLIENT)
public final class AttackSlashClientEvents {
    private AttackSlashClientEvents() {
    }

    @SubscribeEvent
    public static void onPlayerSwing(PlayerInteractEvent.LeftClickEmpty event) {
        PacketDistributor.sendToServer(new PlayerSwingPayload());
    }
}
