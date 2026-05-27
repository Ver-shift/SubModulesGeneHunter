package org.galaxy.beyond.data.progress;

import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition.*;

import java.util.List;

import static org.galaxy.beyond.api.datagen.custom.RogueProgressProvider.*;

/**
 * 教程关卡 —— 2 场景，简单遭遇池。
 * <p>
 * 规则：
 * 1) 怪物遭遇末尾跟上 reward 事件
 * 2) Boss 遭遇固定 3 事件：商店 + Boss + 奖励
 */
public class TutorialProgress extends BaseProgress {

    @Override
    protected List<SceneRoll> scenes() {
        return List.of(
                SceneRoll.of(1, SceneEntry.of(SceneType.HARVEST, 1)),
                SceneRoll.of(2, SceneEntry.of(SceneType.REPOSE, 1)),
                SceneRoll.of(3,SceneEntry.of(SceneType.CLIMAX,1)),
                SceneRoll.of(4,SceneEntry.of(SceneType.CLIMAX,1))
        );
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
