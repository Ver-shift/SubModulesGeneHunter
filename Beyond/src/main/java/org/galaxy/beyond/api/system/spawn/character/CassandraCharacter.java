package org.galaxy.beyond.api.system.spawn.character;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.spawn.SpawnBudget;
import org.galaxy.beyond.api.system.spawn.SpawnContext;
import org.galaxy.beyond.api.system.spawn.SpawnDifficultyFormula;
import org.galaxy.beyond.api.system.spawn.SpawnDifficultySettings;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;

public class CassandraCharacter extends Character {
    public static final ResourceLocation ID = Beyond.asResource("cassandra");
    private final SpawnDifficultySettings difficultySettings;
    private final SpawnDifficultyFormula difficultyFormula = new SpawnDifficultyFormula();

    public CassandraCharacter() {
        this(SpawnDifficultySettings.DEFAULT);
    }

    public CassandraCharacter(SpawnDifficultySettings difficultySettings) {
        super(ID);
        this.difficultySettings = difficultySettings;
    }

    @Override
    public SpawnBudget createBudget(SpawnDefinition definition, SpawnContext context) {
        return difficultyFormula.createBudget(
                difficultySettings,
                currentStageOrder(context),
                currentLayer(context),
                definition.getBaseValue(),
                definition.getMaxSpawnCount(),
                colorMultiplier(context)
        );
    }
}
