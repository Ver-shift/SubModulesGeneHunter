package org.galaxy.beyond.api.system.spawn.character;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.spawn.SpawnBudget;
import org.galaxy.beyond.api.system.spawn.SpawnContext;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;
import org.galaxy.beyond.api.system.spawn.SpawnSessionData;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.List;

/**
 * 刷怪角色抽象基类。
 */
public abstract class Character {

    private final ResourceLocation id;

    public Character(final ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public abstract SpawnBudget createBudget(SpawnDefinition definition, SpawnContext context);

    public int currentStageOrder(SpawnContext context) {
        return Math.max(1, context.stageIndex() + 1);
    }

    public int currentLayer(SpawnContext context) {
        int layer = 1;
        int stageIndex = Math.clamp(context.stageIndex(), 0, context.scenes().size());
        for (int i = 0; i < stageIndex; i++) {
            if (context.scenes().get(i) == SceneType.CLIMAX) {
                layer++;
            }
        }
        return layer;
    }

    public float colorMultiplier(SpawnContext context) {
        return switch (context.nodeColor()) {
            case GREEN -> 0.5F;
            case ORANGE -> 1.0F;
            case RED -> 1.5F;
            default -> 1.0F;
        };
    }

    public int waveCount(SpawnDefinition definition, SpawnContext context) {
        return 3;
    }

    public SpawnSessionData createSpawnData(SpawnDefinition definition, SpawnContext context, SpawnPlan plan) {
        return SpawnSessionData.of(definition.getId(), getId(), context, plan);
    }

    public Object createSpawnToken(SpawnDefinition definition, SpawnPlan plan) {
        return null;
    }

    public Object placeSpawn(
            SpawnDefinition definition,
            SpawnPlan plan,
            ServerLevel level,
            BlockPos nodePos,
            List<ServerPlayer> players
    ) {
        if (players.isEmpty()) {
            return null;
        }
        return createSpawnToken(definition, plan);
    }

    public boolean hasActiveSpawn(SpawnDefinition definition, ServerLevel level, BlockPos nodePos) {
        return false;
    }

}
