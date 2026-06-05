package org.galaxy.gene_hunter.api.system.choice;

import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.data.LootPoolDefinition;
import org.galaxylib.api.system.loot.data.LootTableDefinition;

public record XeneChoiceWeightModifier(NodeColor nodeColor) implements LootManager.WeightModifier {

    private static final String BASIC_POOL = "basic_attribute";
    private static final String NORMAL_POOL = "normal_attribute";
    private static final String ADVANCED_POOL = "advanced_attribute";

    @Override
    public int modifyPoolWeight(LootTableDefinition table, LootPoolDefinition pool, LootManager.Context context) {
        return switch (color()) {
            case RED -> redWeight(pool);
            case ORANGE -> orangeWeight(pool);
            default -> greenWeight(pool);
        };
    }

    private NodeColor color() {
        return nodeColor == null ? NodeColor.GREEN : nodeColor;
    }

    private int greenWeight(LootPoolDefinition pool) {
        return switch (pool.name()) {
            case BASIC_POOL -> pool.baseWeight() * 3;
            case NORMAL_POOL -> pool.baseWeight() * 2;
            default -> pool.baseWeight();
        };
    }

    private int orangeWeight(LootPoolDefinition pool) {
        return pool.baseWeight();
    }

    private int redWeight(LootPoolDefinition pool) {
        return switch (pool.name()) {
            case NORMAL_POOL -> pool.baseWeight() * 2;
            case ADVANCED_POOL -> pool.baseWeight() * 4;
            default -> pool.baseWeight();
        };
    }
}
