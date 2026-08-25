package org.galaxy.beyond.data.progress;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.definition.ProgressDefinition;
import org.galaxy.beyond.api.system.definition.ProgressDefinition.*;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseProgress {

    public ProgressDefinition build() {
        return ProgressDefinition.builder()
                .spawnDefinition(spawnDefinition())
                .lootTables(lootTables())
                .progressCaps(progressCaps())
                .sceneRolls(scenes())
                .encounters(encounters())
                .build();
    }

    protected abstract List<SceneRoll> scenes();

    protected ResourceLocation spawnDefinition() {
        return CassandraCharacter.ID;
    }

    protected List<ResourceLocation> progressCaps() {
        return List.of();
    }

    /** 关卡战利品表配置；默认为空，由玩法模块按需提供兼容默认值。 */
    protected Map<ResourceLocation, List<ResourceLocation>> lootTables() {
        return Map.of();
    }

    protected List<Encounter> greenEncounters() {
        return List.of();
    }

    protected List<Encounter> orangeEncounters() {
        return List.of();
    }

    protected List<Encounter> redEncounters() {
        return List.of();
    }

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
