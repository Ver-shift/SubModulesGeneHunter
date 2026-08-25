package org.galaxy.gene_hunter.api.system.choice;

import net.minecraft.server.level.ServerPlayer;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;

import java.util.List;

public class ChoiceRollFactory {

    public LootManager.Request create(ServerPlayer player, ChoiceStage stage) {
        ILootType<?> lootType = stage.lootType();
        LootManager.Request.RequestBuilder builder = LootManager.Request.builder()
                .lootType(lootType)
                .replacement(false);

        if (lootType == GeneHunterLootInit.WEAPON_LOOT_TYPE.get()) {
            List<net.minecraft.resources.ResourceLocation> configured = currentTables(player, GalaxyLibLootTypeInit.getLootTypeId(GeneHunterLootInit.WEAPON_LOOT_TYPE.get()));
            return builder.tables(configured.isEmpty() ? List.of(
                    GeneHunter.asResource("blade_weapon_base"),
                    GeneHunter.asResource("sword_weapon_base"),
                    GeneHunter.asResource("axe_weapon_base"),
                    GeneHunter.asResource("hammer_weapon_base"),
                    GeneHunter.asResource("polearm_weapon_base")) : configured)
                    .rolls(rolls(player, stage, 1))
                    .build();
        }

        if (lootType == BiotechLootTypeInit.XENE_LOOT_TYPE.get()) {
            List<net.minecraft.resources.ResourceLocation> configured = currentTables(player, GalaxyLibLootTypeInit.getLootTypeId(BiotechLootTypeInit.XENE_LOOT_TYPE.get()));
            return builder
                    .tables(configured.isEmpty() ? List.of(GeneHunter.asResource("xene_trait_base")) : configured)
                    .weightModifier(new XeneChoiceWeightModifier(stage.nodeColor()))
                    .rolls(rolls(player, stage, lootType.getPoolCount(player)))
                    .build();
        }

        return builder.rolls(rolls(player, stage, 1)).build();
    }

    private List<net.minecraft.resources.ResourceLocation> currentTables(ServerPlayer player, net.minecraft.resources.ResourceLocation lootTypeId) {
        return BeyondAPI.getCurrentProgressDefinition(player.serverLevel())
                .map(definition -> definition.getLootTables(lootTypeId))
                .orElse(List.of());
    }

    private int rolls(ServerPlayer player, ChoiceStage stage, int defaultRolls) {
        return stage.rollsOrDefault(defaultRolls, LootManager.Context.of(player).random());
    }
}
