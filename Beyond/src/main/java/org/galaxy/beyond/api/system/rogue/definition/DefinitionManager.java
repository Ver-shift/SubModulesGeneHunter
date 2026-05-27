package org.galaxy.beyond.api.system.rogue.definition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.event.custom.ResolveEvent;
import org.galaxy.beyond.api.pack.ProgressDataPack;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.BeyondGlobalData;
import org.galaxy.beyond.api.system.rogue.*;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.Encounter;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.SceneRoll;

import java.util.ArrayList;
import java.util.List;

public class DefinitionManager implements IDefinitionManager {

    @Override
    public void onServerStarted(MinecraftServer server) {
        ProgressDataPack.applyPending(server);
    }

    @Override
    public EventTask resolveEvent(ServerLevel level, EncounterType encounterType) {
        List<ResourceLocation> allEventIds = new ArrayList<>();

        BeyondGlobalData globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
        ProgressType currentProgress = BeyondAPI.getProgressType(BeyondAPI.getOverWorld());
        if (currentProgress.getId() == null) return new EventTask(allEventIds);
        var def = globalData.getRogueDefinition();
        var error = def.validateProgress(currentProgress.getId());
        if (error != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(error, false);
            return new EventTask(allEventIds);
        }
        ProgressDefinition definition = def.getProgress(currentProgress.getId());
        RandomSource random = globalData.getRogueRandom().getProgressRandom();

        boolean found = false;
        for (Encounter encounter : definition.getEncounters()) {
            if (encounter.getType() != encounterType) continue;
            found = true;

            WeightedRandomList<WeightedEntry.Wrapper<EventTask>> tasks = encounter.eventsAsWeightedList();
            if (tasks.isEmpty()) continue;

            EventTask task = tasks.getRandom(random).map(WeightedEntry.Wrapper::data).orElse(null);
            if (task == null) continue;

            ResolveEvent.ResolveEventTaskEvent event = new ResolveEvent.ResolveEventTaskEvent(level, encounter, task);
            var event1 = NeoForge.EVENT_BUS.post(event);
            EventTask resolved = event1.getTo();
            if (resolved != null) {
                allEventIds.addAll(resolved.getEvents());
            }
        }

        if (!found) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "beyond.definition.missing_encounter", encounterType.name(), currentProgress.getId()), false);
        }

        return new EventTask(allEventIds);
    }

    @Override
    public List<SceneType> resolveScenes(ServerLevel level) {
        BeyondGlobalData globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
        ProgressType currentProgress = BeyondAPI.getProgressType(BeyondAPI.getOverWorld());
        if (currentProgress.getId() == null) return List.of();
        var def = globalData.getRogueDefinition();
        var error = def.validateProgress(currentProgress.getId());
        if (error != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(error, false);
            return List.of();
        }
        ProgressDefinition definition = def.getProgress(currentProgress.getId());
        RandomSource random = globalData.getRogueRandom().getProgressRandom();

        // 按 order 排序后，每个 roll 从自身加权条目中抽取 1 个 SceneType
        List<SceneType> result = new ArrayList<>();
        List<SceneRoll> rolls = definition.sceneRollsOrdered();
        for (SceneRoll roll : rolls) {
            WeightedRandomList<WeightedEntry.Wrapper<SceneType>> list = roll.asWeightedList();
            if (!list.isEmpty()) {
                list.getRandom(random).map(WeightedEntry.Wrapper::data).ifPresent(result::add);
            }
        }

        ResolveEvent.ResolveSceneEvent event = new ResolveEvent.ResolveSceneEvent(level, rolls, result);
        var event1 = NeoForge.EVENT_BUS.post(event);
        List<SceneType> resolved = event1.getTo();

        return resolved != null ? resolved : List.of();
    }
}
