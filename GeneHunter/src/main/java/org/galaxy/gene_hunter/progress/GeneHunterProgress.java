package org.galaxy.gene_hunter.progress;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.*;
import org.galaxy.beyond.data.progress.ForestProgress;
import org.galaxy.gene_hunter.rogue_event.RogueRewardEventType;

import java.util.ArrayList;
import java.util.List;

public class GeneHunterProgress extends ForestProgress {

    @Override
    protected List<SceneRoll> scenes() {
        return List.of(
                harvest(1),
                harvest(2),
                harvest(3),
                harvest(4),
                repose(5),
                harvest(6),
                harvest(7),
                harvest(8),
                harvest(9),
                repose(10),
                harvest(11),
                harvest(12),
                harvest(13),
                harvest(14),
                climax(15)
        );
    }

    @Override
    protected List<Encounter> greenEncounters() {
        return withRogueReward(super.greenEncounters());
    }

    @Override
    protected List<Encounter> orangeEncounters() {
        return withRogueReward(super.orangeEncounters());
    }

    @Override
    protected List<Encounter> redEncounters() {
        return withRogueReward(super.redEncounters());
    }

    private static SceneRoll harvest(int order) {
        return SceneRoll.of(order, SceneEntry.of(SceneType.HARVEST, 1));
    }

    private static SceneRoll repose(int order) {
        return SceneRoll.of(order, SceneEntry.of(SceneType.REPOSE, 1));
    }

    private static SceneRoll climax(int order) {
        return SceneRoll.of(order, SceneEntry.of(SceneType.CLIMAX, 1));
    }

    private static List<Encounter> withRogueReward(List<Encounter> encounters) {
        List<Encounter> result = new ArrayList<>(encounters.size());
        for (Encounter encounter : encounters) {
            result.add(Encounter.builder()
                    .type(encounter.getType())
                    .events(encounter.getEvents().stream()
                            .map(roll -> EventRoll.of(roll.getWeight(), withRogueReward(roll.getTask())))
                            .toList())
                    .build());
        }
        return result;
    }

    private static EventTask withRogueReward(EventTask task) {
        List<ResourceLocation> eventIds = new ArrayList<>(task.getEventIds().size());
        for (ResourceLocation id : task.getEventIds()) {
            eventIds.add(id.getPath().equals("reward") ? RogueRewardEventType.ID : id);
        }
        return new EventTask(eventIds);
    }
}
