package org.galaxy.beyond.data.progress;

import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.*;

import java.util.List;

import static org.galaxy.beyond.api.datagen.custom.RogueProgressProvider.*;

/**
 * 森林关卡 —— 3 场景，加权遭遇池。
 * <p>
 * 规则：
 * 1) 怪物遭遇末尾跟上 reward 事件
 * 2) Boss 遭遇固定 3 事件：商店 + Boss + 奖励
 */
public class ForestProgress extends TutorialProgress {

    @Override
    protected List<SceneRoll> scenes() {
        return List.of(
                SceneRoll.of(1,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(2,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(3,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(4,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(5,  SceneEntry.of(SceneType.REPOSE, 1)),
                SceneRoll.of(6,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(7,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(8,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(9,  SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(10, SceneEntry.of(SceneType.CLIMAX, 1))
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
                        evt("monster", "monster", "reward"), 3,
                        evt("boss", "reward"), 2),
                enc(EncounterType.Orange_NormalShop, evt("shop")),
                enc(EncounterType.Orange_BossShop,   evt("shop", "boss", "reward"))
        );
    }

    @Override
    protected List<Encounter> redEncounters() {
        return List.of(
                enc(EncounterType.Red_EliteMonster,
                        evt("monster", "monster", "reward"), 4,
                        evt("boss", "reward"), 1),
                enc(EncounterType.Red_CursedShop,
                        evt("shop", "monster", "reward"), 3,
                        evt("shop"), 1),
                enc(EncounterType.Red_BossShop, evt("shop", "boss", "reward"))
        );
    }
}
