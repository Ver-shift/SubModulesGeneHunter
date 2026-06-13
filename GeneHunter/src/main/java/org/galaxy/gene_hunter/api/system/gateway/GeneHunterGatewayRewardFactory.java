package org.galaxy.gene_hunter.api.system.gateway;

import dev.shadowsoffire.gateways.gate.Reward;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;

import java.util.List;

public final class GeneHunterGatewayRewardFactory {

    private GeneHunterGatewayRewardFactory() {
    }

    public static List<Reward> create(SpawnPlan plan) {
        int raidValue = plan.budget().totalValue();
        return List.of(new ChoiceReward(raidValue, plan.boss()));
    }
}
