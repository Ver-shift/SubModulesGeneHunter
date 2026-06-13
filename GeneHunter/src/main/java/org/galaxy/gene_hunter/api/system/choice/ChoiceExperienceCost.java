package org.galaxy.gene_hunter.api.system.choice;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxylib.api.system.loot.LootManager;

import java.util.Map;

public class ChoiceExperienceCost {

    private static final int DEFAULT_COST = 10;

    private static final Map<ResourceLocation, Integer> TABLE_COSTS = Map.of(
            GeneHunter.asResource("xene_trait_base"), 10,
            GeneHunter.asResource("one_hand_weapon_base"), 25,
            GeneHunter.asResource("two_hand_weapon_base"), 30,
            GeneHunter.asResource("polearm_weapon_base"), 30
    );

    public int nextCost(LootManager.Request request, int refreshTimes) {
        int baseCost = request.getTables().stream()
                .mapToInt(table -> TABLE_COSTS.getOrDefault(table, DEFAULT_COST))
                .max()
                .orElse(DEFAULT_COST);
        return baseCost * (refreshTimes + 1);
    }

    public boolean consume(ServerPlayer player, int points) {
        if (points <= 0) {
            return true;
        }
        if (player.totalExperience < points) {
            return false;
        }
        player.giveExperiencePoints(-points);
        return true;
    }
}
