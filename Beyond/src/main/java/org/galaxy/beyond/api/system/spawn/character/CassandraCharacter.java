package org.galaxy.beyond.api.system.spawn.character;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.spawn.SpawnBudget;
import org.galaxy.beyond.api.system.spawn.SpawnContext;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;

public class CassandraCharacter extends Character {
    public static final ResourceLocation ID = Beyond.asResource("cassandra");

    public CassandraCharacter() {
        super(ID);
    }

    @Override
    public SpawnBudget createBudget(SpawnDefinition definition, SpawnContext context) {
        int index = Math.max(0, context.stageIndex());
        int multiplier = colorMultiplier(definition, context.nodeColor());
        int value = definition.getBaseValue() + index * definition.getValueGrowth();
        int totalValue = value * multiplier / 100;
        return new SpawnBudget(totalValue, definition.getMaxSpawnCount(), multiplier);
    }

    protected int colorMultiplier(SpawnDefinition definition, NodeColor color) {
        return switch (color) {
            case GREEN -> definition.getGreenValue();
            case ORANGE -> definition.getOrangeValue();
            case RED -> definition.getRedValue();
            default -> 100;
        };
    }
}
