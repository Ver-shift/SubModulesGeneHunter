package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.spawn.EncounterSpawnPlanner;
import org.galaxy.beyond.api.system.spawn.SpawnContext;
import org.galaxy.beyond.api.system.spawn.SpawnSessionData;

public final class SpawnSessionResolver {

    public SpawnSessionData resolve(ServerLevel level, IRogueContext context, RogueNodeData nodeData,
                                    EncounterType encType, RogueEventType currentEvent) {
        var rogueData = context.getRogueData(level);
        boolean boss = isBossEvent(encType, currentEvent);
        if (rogueData.getCurrentSpawn() != null && rogueData.getCurrentSpawn().isBoss() == boss) {
            return rogueData.getCurrentSpawn();
        }

        var definitionId = BeyondAPI.resolveSpawnDefinition(level);
        if (definitionId == null) {
            return SpawnSessionData.empty();
        }

        var definition = BeyondAPI.getSpawnDefinition(definitionId).orElse(null);
        if (definition == null) {
            return SpawnSessionData.empty();
        }

        var progressType = rogueData.getProgressType();
        SpawnContext spawnContext = new SpawnContext(
                progressType.getClampedScenesIndex(),
                encType.getColor(),
                level.random,
                progressType.getScenes()
        );
        var character = BeyondAPI.getSpawnCharacter(definition);
        var plan = new EncounterSpawnPlanner(character).createPlan(definition, spawnContext, boss);
        SpawnSessionData spawnData = character.createSpawnData(definition, spawnContext, plan);
        rogueData.setCurrentSpawn(spawnData);
        return spawnData;
    }

    private boolean isBossEvent(EncounterType encType, RogueEventType event) {
        return encType.getSceneType() == SceneType.CLIMAX && event != null && "boss".equals(event.getId().getPath());
    }
}
