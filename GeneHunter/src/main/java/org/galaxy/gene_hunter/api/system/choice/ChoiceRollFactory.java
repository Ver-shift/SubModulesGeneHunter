package org.galaxy.gene_hunter.api.system.choice;

import net.minecraft.server.level.ServerPlayer;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.List;

public class ChoiceRollFactory {

    public LootManager.Request create(ServerPlayer player, ChoiceStage stage) {
        ILootType<?> lootType = stage.lootType();
        LootManager.Request.RequestBuilder builder = LootManager.Request.builder()
                .lootType(lootType)
                .replacement(false);

        if (lootType == GeneHunterLootInit.WEAPON_LOOT_TYPE.get()) {
            return builder
                    .tables(List.of(
                            GeneHunter.asResource("one_hand_weapon_base"),
                            GeneHunter.asResource("two_hand_weapon_base"),
                            GeneHunter.asResource("polearm_weapon_base")
                    ))
                    .rolls(rolls(player, stage, 1))
                    .build();
        }

        if (lootType == BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get()) {
            return builder
                    .tables(List.of(GeneHunter.asResource("xene_trait_base")))
                    .weightModifier(new XeneChoiceWeightModifier(stage.nodeColor()))
                    .rolls(rolls(player, stage, lootType.getPoolCount(player)))
                    .build();
        }

        return builder.rolls(rolls(player, stage, 1)).build();
    }

    private int rolls(ServerPlayer player, ChoiceStage stage, int defaultRolls) {
        return stage.rollsOrDefault(defaultRolls, LootManager.Context.of(player).random());
    }
}
