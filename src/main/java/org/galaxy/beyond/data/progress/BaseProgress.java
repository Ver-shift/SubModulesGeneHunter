package org.galaxy.beyond.data.progress;

import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.*;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseProgress {

    public ProgressDefinition build() {
        return ProgressDefinition.builder()
                .sceneRolls(scenes())
                .encounters(encounters())
                .build();
    }

    protected abstract List<SceneRoll> scenes();

    protected List<Encounter> greenEncounters()  { return List.of(); }
    protected List<Encounter> orangeEncounters() { return List.of(); }
    protected List<Encounter> redEncounters()    { return List.of(); }

    protected List<Encounter> encounters() {
        List<Encounter> all = new ArrayList<>();
        all.addAll(greenEncounters());
        all.addAll(orangeEncounters());
        all.addAll(redEncounters());
        return all;
    }

    protected static SceneRoll harvest(int order) {
        return SceneRoll.of(order, SceneEntry.of(SceneType.HARVEST, 1));
    }

    protected static SceneRoll repose(int order) {
        return SceneRoll.of(order, SceneEntry.of(SceneType.REPOSE, 1));
    }

    protected static SceneRoll climax(int order) {
        return SceneRoll.of(order, SceneEntry.of(SceneType.CLIMAX, 1));
    }
}
