package org.galaxy.gene_hunter.api.system.choice;

import net.minecraft.server.level.ServerPlayer;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;

import java.util.List;

public final class GeneHunterChoiceRewards {

    private GeneHunterChoiceRewards() {
    }

    public static void normal(ServerPlayer player, NodeColor nodeColor) {
        start(player, normalScheme(nodeColor));
    }

    public static void boss(ServerPlayer player, NodeColor nodeColor) {
        start(player, bossScheme(nodeColor));
    }

    private static void start(ServerPlayer player, ChoiceRewardScheme scheme) {
        if (scheme.isEmpty()) {
            return;
        }
        GeneHunterAPI.choiceManager().startStages(player, scheme.stages());
    }

    private static ChoiceRewardScheme normalScheme(NodeColor nodeColor) {
        return new ChoiceRewardScheme(List.of(
                ChoiceStage.of(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get(), nodeColor)
        ));
    }

    private static ChoiceRewardScheme bossScheme(NodeColor nodeColor) {
        return new ChoiceRewardScheme(List.of(
                ChoiceStage.fixed(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get(), nodeColor, 2.1F),
                ChoiceStage.of(GeneHunterLootInit.WEAPON_LOOT_TYPE.get(), nodeColor)
        ));
    }
}
