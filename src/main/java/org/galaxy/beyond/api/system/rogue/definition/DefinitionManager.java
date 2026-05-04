package org.galaxy.beyond.api.system.rogue.definition;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.event.custom.ResolveEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.NodeChangeEvent;

public class DefinitionManager implements IDefinitionManager {


    @Override
    public EventTask resolveEvent(ServerLevel level, EncounterType encounterType) {

        List<RogueEventType> eventTypes = new ArrayList<>();

        RogueDefinition rogueDefinition = BeyondAPI.getGlobalData(level.getServer()).getRogueDefinition();
        ProgressType currentProgress = BeyondAPI.getBeyondLevelData(level).getRogueData().getProgressType();
        ProgressDefinition definition = rogueDefinition.getRogueProgress().get(currentProgress.getId());
        List<ProgressDefinition.EncounterEntry> encounterEntries = definition.getEventTasks();

        for (ProgressDefinition.EncounterEntry encounterEntry : encounterEntries){
            //todo 根据种子系统。进行抽取。
        }

        ResolveEvent.ResolveEventTaskEvent event = new ResolveEvent.ResolveEventTaskEvent();
        NeoForge.EVENT_BUS.post(event);

        EventTask task = new EventTask(eventTypes);
        return task;
    }

    @Override
    public List<SceneType> resolveScenes(ServerLevel level) {

        RogueDefinition rogueDefinition = BeyondAPI.getGlobalData(level.getServer()).getRogueDefinition();
        ProgressType currentProgress = BeyondAPI.getBeyondLevelData(level).getRogueData().getProgressType();
        ProgressDefinition definition = rogueDefinition.getRogueProgress().get(currentProgress.getId());

        List<ProgressDefinition.SceneEntry> sceneEntries = definition.getScenes();
        for (ProgressDefinition.SceneEntry sceneEntry : sceneEntries){
            //todo 根据种子系统。进行抽取。
        }

        ResolveEvent.ResolveSceneEvent event = new ResolveEvent.ResolveSceneEvent();
        NeoForge.EVENT_BUS.post(event);

        return List.of();
    }


}
