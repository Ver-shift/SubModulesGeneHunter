package org.galaxy.gene_hunter.api.system.choice;

import net.minecraft.util.RandomSource;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxylib.api.system.loot.core.ILootType;

public record ChoiceStage(ILootType<?> lootType, NodeColor nodeColor, float fixedRolls) {

    public static ChoiceStage of(ILootType<?> lootType, NodeColor nodeColor) {
        return new ChoiceStage(lootType, nodeColor, 0.0F);
    }

    public static ChoiceStage fixed(ILootType<?> lootType, NodeColor nodeColor, float rolls) {
        return new ChoiceStage(lootType, nodeColor, rolls);
    }

    public int rollsOrDefault(int defaultRolls, RandomSource random) {
        return fixedRolls > 0.0F ? ILootType.getPoolCountFromAttribute(fixedRolls, random) : defaultRolls;
    }
}
