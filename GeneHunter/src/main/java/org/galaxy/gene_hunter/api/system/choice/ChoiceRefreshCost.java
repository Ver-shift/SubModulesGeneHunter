package org.galaxy.gene_hunter.api.system.choice;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.gene_hunter.GeneHunter;

import java.util.List;
import java.util.Map;

public final class ChoiceRefreshCost {

    private static final int DEFAULT_COST = 10;

    private static final Map<ResourceLocation, Integer> TABLE_COSTS = Map.of(
            GeneHunter.asResource("xene_trait_base"), 10,
            GeneHunter.asResource("one_hand_weapon_base"), 25,
            GeneHunter.asResource("two_hand_weapon_base"), 30,
            GeneHunter.asResource("polearm_weapon_base"), 30
    );

    private ChoiceRefreshCost() {
    }

    public static int nextCost(List<ResourceLocation> tables, int refreshTimes) {
        int baseCost = tables.stream()
                .mapToInt(table -> TABLE_COSTS.getOrDefault(table, DEFAULT_COST))
                .max()
                .orElse(DEFAULT_COST);
        return baseCost * (refreshTimes + 1);
    }
}
