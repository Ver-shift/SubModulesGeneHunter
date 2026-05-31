package org.galaxy.gene_hunter.api.init;

import dev.shadowsoffire.gateways.gate.Reward;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.gateway.ChoiceReward;

public class GeneHunterGatewayInit {

    public static void register(IEventBus eventBus) {
        Reward.CODEC.register(GeneHunter.asResource("choice"), ChoiceReward.CODEC);
    }

    private GeneHunterGatewayInit() {
    }
}
