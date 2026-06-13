package org.galaxy.gene_hunter.api.init;

import dev.shadowsoffire.gateways.gate.Reward;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.gateway.ChoiceReward;
import org.galaxy.gene_hunter.gateway.GeneHunterDynamicGatewayManager;

@EventBusSubscriber
public class GeneHunterGatewayInit {

    public static void register(IEventBus eventBus) {
        Reward.CODEC.register(GeneHunter.asResource("choice"), ChoiceReward.CODEC);
        GeneHunterDynamicGatewayManager.bootstrap();
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GeneHunterDynamicGatewayManager.restoreIfNeeded(event.getServer().overworld());
        GeneHunterDynamicGatewayManager.syncToAll();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        GeneHunterDynamicGatewayManager.clear();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            GeneHunterDynamicGatewayManager.restoreIfNeeded(player.serverLevel().getServer().overworld());
            GeneHunterDynamicGatewayManager.syncToPlayer(player);
        }
    }

    private GeneHunterGatewayInit() {
    }
}
