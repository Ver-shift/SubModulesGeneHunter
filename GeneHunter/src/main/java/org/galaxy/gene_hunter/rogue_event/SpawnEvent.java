package org.galaxy.gene_hunter.rogue_event;

import lombok.NonNull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.definition.SpawnDefinitionManager;

public abstract class SpawnEvent extends RogueEventType {

    public SpawnEvent(ResourceLocation id) {
        super(id);
    }

    @Override
    public void cast(Context context) {
        var level = context.level();
        var players = context.rogueContext().playersInRogue(level);
        if (players.isEmpty()) return;

        var definition = SpawnDefinitionManager.getDefinitionOrThrow(spawnDefinitionId(context));
        var character = SpawnDefinitionManager.getCharacter(definition);
        var spawnData = context.spawnData();
        sendRaidValueMessage(context);
        Object spawned = character.placeSpawn(definition, spawnData.plan(), level, context.nodePos(), players);
        if (spawnData.plan().spawnCount() > 0 && spawned instanceof java.util.Collection<?> collection && collection.isEmpty()) {
            throw new IllegalStateException("Failed to place spawn for definition: " + definition.getId());
        }
    }

    private void sendRaidValueMessage(Context context) {
        context.rogueContext().playersInRogue(context.level()).forEach(player ->
                player.sendSystemMessage(Component.translatable("gene_hunter.event.raid_value", context.totalValue())));
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
