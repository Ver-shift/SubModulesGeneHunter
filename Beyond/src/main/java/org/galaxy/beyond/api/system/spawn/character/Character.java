package org.galaxy.beyond.api.system.spawn.character;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.spawn.SpawnBudget;
import org.galaxy.beyond.api.system.spawn.SpawnContext;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;

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
