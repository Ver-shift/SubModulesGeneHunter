package org.galaxy.beyond.api.system.rogue.definition;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.event.custom.ResolveEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.BeyondGlobalData;
import org.galaxy.beyond.api.system.rogue.*;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.EncounterEntry;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.EventTaskEntry;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.SceneEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class DefinitionManager implements IDefinitionManager {

    @Override
    public EventTask resolveEvent(ServerLevel level, EncounterType encounterType) {
        List<RogueEventType> allEvents = new ArrayList<>();

        BeyondGlobalData globalData = BeyondAPI.getGlobalData(level.getServer());
        SingleThreadedRandomSource random = globalData.getRogueRandom().getProgressRandom();
        ProgressType currentProgress = BeyondAPI.getBeyondLevelData(level).getRogueData().getProgressType();
        ProgressDefinition definition = globalData.getRogueDefinition().getRogueProgress().get(currentProgress.getId());
        List<EncounterEntry> encounterEntries = definition.getEventTasks();

        for (EncounterEntry encounterEntry : encounterEntries) {
            if (encounterEntry.getEncounterType() != encounterType) continue;

            EventTaskEntry taskEntry = pickByWeight(encounterEntry.getEventTasks(), EventTaskEntry::getWeight, random);
            EventTask task = taskEntry.getEventTasks();
            ResolveEvent.ResolveEventTaskEvent event = new ResolveEvent.ResolveEventTaskEvent(level, encounterEntry, task);
            var event1 = NeoForge.EVENT_BUS.post(event);
            EventTask resolved = event1.getTo();
            if (resolved != null) {
                allEvents.addAll(resolved.getEvents());
            }
        }

        return new EventTask(allEvents);
    }

    @Override
    public List<SceneType> resolveScenes(ServerLevel level) {
        BeyondGlobalData globalData = BeyondAPI.getGlobalData(level.getServer());
        SingleThreadedRandomSource random = globalData.getRogueRandom().getProgressRandom();
        ProgressType currentProgress = BeyondAPI.getBeyondLevelData(level).getRogueData().getProgressType();
        ProgressDefinition definition = globalData.getRogueDefinition().getRogueProgress().get(currentProgress.getId());
        List<SceneEntry> sceneEntries = definition.getScenes();

        SceneEntry selected = pickByWeight(sceneEntries, SceneEntry::getWeight, random);
        List<SceneType> result = new ArrayList<>(selected.getSceneTypes());

        ResolveEvent.ResolveSceneEvent event = new ResolveEvent.ResolveSceneEvent(level, sceneEntries, result);
        var event1 = NeoForge.EVENT_BUS.post(event);
        List<SceneType> resolved = event1.getTo();

        return resolved != null ? resolved : List.of();
    }

    private static <T> T pickByWeight(List<T> entries, ToIntFunction<T> weightFn, SingleThreadedRandomSource random) {
        int totalWeight = 0;
        for (T e : entries) {
            totalWeight += weightFn.applyAsInt(e);
        }
        int roll = totalWeight > 0 ? random.nextInt(totalWeight) : 0;
        int cumulative = 0;
        for (T e : entries) {
            cumulative += weightFn.applyAsInt(e);
            if (roll < cumulative) {
                return e;
            }
        }
        return entries.getFirst();
    }
}
