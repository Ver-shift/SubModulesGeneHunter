package org.galaxy.beyond.data.progress;

import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.definition.ProgressDefinition.*;

import java.util.List;

import static org.galaxy.beyond.api.datagen.custom.RogueProgressProvider.*;

public class TutorialProgress extends BaseProgress {

    @Override
    protected List<SceneRoll> scenes() {
        return List.of(harvest(1), repose(2), climax(3), climax(4));
    }

    @Override
    protected List<Encounter> greenEncounters() {
        return List.of(
                enc(EncounterType.Green_Event,    evt("monster", "reward")),
                enc(EncounterType.Green_Bonfire,  evt("heal")),
                enc(EncounterType.Green_BossShop, evt("shop", "boss", "reward"))
        );
    }

    @Override
    protected List<Encounter> orangeEncounters() {
        return List.of(
                enc(EncounterType.Orange_NormalMonster, evt("monster", "reward")),
                enc(EncounterType.Orange_NormalShop,    evt("shop")),
                enc(EncounterType.Orange_BossShop,      evt("shop", "boss", "reward"))
        );
    }

    @Override
    protected List<Encounter> redEncounters() {
        return List.of(
                enc(EncounterType.Red_EliteMonster, evt("monster", "reward")),
                enc(EncounterType.Red_CursedShop,   evt("shop")),
                enc(EncounterType.Red_BossShop,     evt("shop", "boss", "reward"))
        );
    }
}
