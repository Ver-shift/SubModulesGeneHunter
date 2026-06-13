package org.galaxy.beyond.api.system.definition;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.event.custom.ResolveEvent;
import org.galaxy.beyond.api.init.BeyondRegistries;
import org.galaxy.beyond.api.pack.ProgressDataPack;
import org.galaxy.beyond.api.pack.SpawnDataPack;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.BeyondGlobalData;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.ProgressType;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;
import org.galaxy.beyond.api.system.spawn.character.Character;
import org.galaxy.beyond.api.system.definition.ProgressDefinition.Encounter;
import org.galaxy.beyond.api.system.definition.ProgressDefinition.SceneRoll;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认 Definition 解析器。
 */
public class DefinitionManager implements IDefinitionManager {

    @Override
    public void onServerStarted(MinecraftServer server) {
        ProgressDataPack.applyPending(server);
        SpawnDataPack.applyPending(server);
    }

    @Override
    public EventTask resolveEvent(ServerLevel level, EncounterType encounterType) {
        List<ResourceLocation> allEventIds = new ArrayList<>();
        ProgressDefinition definition = currentProgress(level);
        if (definition == null) {
            return new EventTask(allEventIds);
        }

        RandomSource random = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueRandom().getProgressRandom();
        boolean found = false;
        for (Encounter encounter : definition.getEncounters()) {
            if (encounter.getType() != encounterType) continue;
            found = true;

            WeightedRandomList<WeightedEntry.Wrapper<EventTask>> tasks = encounter.eventsAsWeightedList();
            if (tasks.isEmpty()) continue;

            EventTask task = tasks.getRandom(random).map(WeightedEntry.Wrapper::data).orElse(null);
            if (task == null) continue;

            ResolveEvent.ResolveEventTaskEvent event = new ResolveEvent.ResolveEventTaskEvent(level, encounter, task);
            NeoForge.EVENT_BUS.post(event);
            EventTask resolved = event.getTo();
            if (resolved != null) {
                allEventIds.addAll(resolved.getEvents());
            }
        }

        if (!found) {
            ResourceLocation progressId = currentProgressId();
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.definition.missing_encounter", encounterType.name(), progressId), false);
        }

        return new EventTask(allEventIds);
    }

    @Override
    public List<SceneType> resolveScenes(ServerLevel level) {
        ProgressDefinition definition = currentProgress(level);
        if (definition == null) {
            return List.of();
        }

        RandomSource random = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueRandom().getProgressRandom();
        List<SceneType> result = new ArrayList<>();
        List<SceneRoll> rolls = definition.sceneRollsOrdered();
        for (SceneRoll roll : rolls) {
            WeightedRandomList<WeightedEntry.Wrapper<SceneType>> list = roll.asWeightedList();
            if (!list.isEmpty()) {
                list.getRandom(random).map(WeightedEntry.Wrapper::data).ifPresent(result::add);
            }
        }

        ResolveEvent.ResolveSceneEvent event = new ResolveEvent.ResolveSceneEvent(level, rolls, result);
        NeoForge.EVENT_BUS.post(event);
        List<SceneType> resolved = event.getTo();
        return resolved != null ? resolved : List.of();
    }

    @Override
    public ResourceLocation resolveSpawnDefinition(ServerLevel level) {
        ProgressDefinition definition = currentProgress(level);
        if (definition == null) {
            return null;
        }
        ResolveEvent.ResolveSpawnDefinitionEvent event = new ResolveEvent.ResolveSpawnDefinitionEvent(
                level,
                definition,
                definition.getSpawnDefinition()
        );
        NeoForge.EVENT_BUS.post(event);
        return event.getTo();
    }

    @Override
    public List<ResourceLocation> resolveProgressCaps(ServerLevel level) {
        ProgressDefinition definition = currentProgress(level);
        if (definition == null) {
            return List.of();
        }
        ResolveEvent.ResolveProgressCapsEvent event = new ResolveEvent.ResolveProgressCapsEvent(
                level,
                definition,
                new ArrayList<>(definition.getProgressCaps())
        );
        NeoForge.EVENT_BUS.post(event);
        List<ResourceLocation> resolved = event.getTo();
        return resolved != null ? resolved : List.of();
    }

    @Override
    public Character getSpawnCharacter(SpawnDefinition definition) {
        ResourceLocation id = definition.getCharacter();
        if (id == null) {
            id = CassandraCharacter.ID;
        }
        Character character = BeyondRegistries.SPAWN_CHARACTER.get(id);
        if (character != null) {
            return character;
        }
        return BeyondRegistries.SPAWN_CHARACTER.get(Beyond.asResource("cassandra"));
    }

    private ProgressDefinition currentProgress(ServerLevel level) {
        BeyondGlobalData globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
        ResourceLocation progressId = currentProgressId();
        var error = globalData.getRogueDefinition().validateProgress(progressId);
        if (error != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(error, false);
            return null;
        }
        return globalData.getRogueDefinition().getProgress(progressId);
    }

    private ResourceLocation currentProgressId() {
        ProgressType currentProgress = BeyondAPI.getProgressType(BeyondAPI.getOverWorld());
        return currentProgress.getId();
    }
}
