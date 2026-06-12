package org.galaxy.gene_hunter.rogue_event;

import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.spawn.EncounterSpawnPlanner;
import org.galaxy.beyond.api.system.spawn.SpawnContext;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.definition.SpawnDefinitionManager;

public abstract class GatewayEvent extends RogueEventType {

    public GatewayEvent(ResourceLocation id) {
        super(id);
    }

    @Override
    public void cast(Context context) {
        var level = context.level();
        var players = context.rogueContext().playersInRogue(level);
        if (players.isEmpty()) return;

        SpawnDefinition definition = SpawnDefinitionManager.getDefinitionOrThrow(spawnDefinitionId(context));
        var character = SpawnDefinitionManager.getCharacter(definition);
        int stageIndex = context.rogueContext().getRogueData(level).getProgressType().getClampedScenesIndex();
        SpawnContext spawnContext = new SpawnContext(stageIndex, context.type().getColor(), level.random);
        var plan = new EncounterSpawnPlanner(character).createPlan(definition, spawnContext);
        character.placeSpawn(definition, plan, level, context.nodePos(), players);
    }

    @Override
    @NonNull
    public Result next(Context context) {
        var definition = SpawnDefinitionManager.getDefinition(spawnDefinitionId(context));
        if (definition.isEmpty()) return Result.FAILURE;
        var character = SpawnDefinitionManager.getCharacter(definition.get());
        return character.hasActiveSpawn(definition.get(), context.level(), context.nodePos())
                ? Result.FAILURE
                : Result.SUCCESS;
    }

    @Override
    public int auto() {
        return 20;
    }

    protected ResourceLocation spawnDefinitionId(Context context) {
        ResourceLocation id = BeyondAPI.getBeyondManager().getDefinitionManager().resolveSpawnDefinition(context.level());
        if (id == null) {
            throw new IllegalStateException("Missing spawn definition for progress: "
                    + context.rogueContext().getRogueData(context.level()).getProgressId());
        }
        return id;
    }

}
