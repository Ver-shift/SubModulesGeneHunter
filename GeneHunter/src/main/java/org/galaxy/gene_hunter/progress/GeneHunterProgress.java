package org.galaxy.gene_hunter.progress;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.definition.ProgressDefinition.*;
import org.galaxy.beyond.data.progress.BaseProgress;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.data.spawn.ZombieExampleSpawnDefinition;

import java.util.List;
import java.util.Map;

import static org.galaxy.beyond.api.datagen.custom.RogueProgressProvider.enc;

public class GeneHunterProgress extends BaseProgress {

    private static final ResourceLocation SHOP = Beyond.asResource("shop");
    private static final ResourceLocation HEAL = Beyond.asResource("heal");
    private static final ResourceLocation MONSTER = GeneHunter.asResource("monster");
    private static final ResourceLocation BOSS = GeneHunter.asResource("boss");
    private static final ResourceLocation SPAWN = ZombieExampleSpawnDefinition.ID;

    private static EventTask task(ResourceLocation... ids) {
        return new EventTask(List.of(ids));
    }

    @Override
    protected List<SceneRoll> scenes() {
        return List.of(
                harvest(1), harvest(2), harvest(3), harvest(4),
                harvest(5), harvest(6), harvest(7), climax(8),
                harvest(9), harvest(10), harvest(11), harvest(12),
                harvest(13), harvest(14), climax(15),
                harvest(16), harvest(17), harvest(18), harvest(19),
                climax(20)
        );
    }

    @Override
    protected ResourceLocation spawnDefinition() {
        return SPAWN;
    }

    @Override
    protected Map<ResourceLocation, List<ResourceLocation>> lootTables() {
        return Map.of(
                GeneHunter.asResource("weapon"), List.of(
                        GeneHunter.asResource("blade_weapon_base"),
                        GeneHunter.asResource("sword_weapon_base"),
                        GeneHunter.asResource("axe_weapon_base"),
                        GeneHunter.asResource("hammer_weapon_base"),
                        GeneHunter.asResource("polearm_weapon_base")),
                ResourceLocation.fromNamespaceAndPath("biotech", "xene"),
                List.of(GeneHunter.asResource("xene_trait_base"))
        );
    }

    @Override
    protected List<Encounter> greenEncounters() {
        return List.of(
                enc(EncounterType.Green_Event,
                        task(MONSTER), 6,
                        task(SHOP), 2,
                        task(HEAL), 2),
                enc(EncounterType.Green_Bonfire, task(HEAL)),
                enc(EncounterType.Green_BossShop, task(SHOP, BOSS))
        );
    }

    @Override
    protected List<Encounter> orangeEncounters() {
        return List.of(
                enc(EncounterType.Orange_NormalMonster,
                        task(MONSTER), 5,
                        task(MONSTER, MONSTER), 5),
                enc(EncounterType.Orange_NormalShop, task(SHOP)),
                enc(EncounterType.Orange_BossShop, task(SHOP, BOSS))
        );
    }

    @Override
    protected List<Encounter> redEncounters() {
        return List.of(
                enc(EncounterType.Red_EliteMonster,
                        task(MONSTER, MONSTER), 5),
                enc(EncounterType.Red_CursedShop,
                        task(SHOP, MONSTER), 3,
                        task(SHOP), 1),
                enc(EncounterType.Red_BossShop, task(SHOP, BOSS))
        );
    }
}
