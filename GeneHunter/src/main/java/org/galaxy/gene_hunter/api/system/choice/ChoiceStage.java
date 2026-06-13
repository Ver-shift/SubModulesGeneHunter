package org.galaxy.gene_hunter.api.system.choice;

import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxylib.api.system.loot.core.ILootType;

public record ChoiceStage(ILootType<?> lootType, NodeColor nodeColor, int fixedRolls) {

    public static ChoiceStage of(ILootType<?> lootType, NodeColor nodeColor) {
        return new ChoiceStage(lootType, nodeColor, 0);
    }

    public static ChoiceStage fixed(ILootType<?> lootType, NodeColor nodeColor, int rolls) {
        return new ChoiceStage(lootType, nodeColor, rolls);
    }

    public int rollsOrDefault(int defaultRolls) {
        return fixedRolls > 0 ? fixedRolls : defaultRolls;
    }
}
