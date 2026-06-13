package org.galaxy.beyond.data.progress;

import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.definition.ProgressDefinition.*;

import java.util.List;

import static org.galaxy.beyond.api.datagen.custom.RogueProgressProvider.*;

public class ForestProgress extends BaseProgress {

    @Override
    protected List<SceneRoll> scenes() {
        return List.of(
                harvest(1), harvest(2), harvest(3), harvest(4),
                repose(5),
                harvest(6), harvest(7), harvest(8), harvest(9),
                climax(10)
        );
    }

    @Override
    protected List<Encounter> greenEncounters() {
        return List.of(
                enc(EncounterType.Green_Event,
                        evt("monster", "reward"), 8,
                        evt("shop"), 2),
                enc(EncounterType.Green_Bonfire,  evt("heal")),
                enc(EncounterType.Green_BossShop, evt("shop", "boss", "reward"))
        );
    }

    @Override
    protected List<Encounter> orangeEncounters() {
        return List.of(
                enc(EncounterType.Orange_NormalMonster,
                        evt("monster", "reward"), 5,
                        evt("monster", "monster", "reward"), 5),
                enc(EncounterType.Orange_NormalShop, evt("shop")),
                enc(EncounterType.Orange_BossShop,   evt("shop", "boss", "reward"))
        );
    }

    @Override
    protected List<Encounter> redEncounters() {
        return List.of(
                enc(EncounterType.Red_EliteMonster,
                        evt("monster", "monster", "reward"), 5),
                enc(EncounterType.Red_CursedShop,
                        evt("shop", "monster", "reward"), 3,
                        evt("shop"), 1),
                enc(EncounterType.Red_BossShop, evt("shop", "boss", "reward"))
        );
    }
}
