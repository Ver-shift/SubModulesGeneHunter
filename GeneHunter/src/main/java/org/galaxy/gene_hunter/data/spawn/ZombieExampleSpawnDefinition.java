package org.galaxy.gene_hunter.data.spawn;

import net.minecraft.world.entity.EntityType;
import org.galaxy.beyond.api.system.spawn.BossSpawnEntry;
import org.galaxy.beyond.api.system.spawn.BossSpawnPack;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.spawn.SpawnEntry;
import org.galaxy.beyond.api.system.spawn.SpawnPack;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.character.GeneHunterSpawnCharacter;

import java.util.List;

public class ZombieExampleSpawnDefinition {

    public static final net.minecraft.resources.ResourceLocation ID = GeneHunter.asResource("zombie_example");

    public SpawnDefinition build() {
        return SpawnDefinition.builder()
                .id(ID)
                .character(GeneHunterSpawnCharacter.ID)
                .maxSpawnCount(12)
                .maxWaveTime(6000)
                .setupTime(40)
                .packs(List.of(
                        pack(6, 5, 0, 0, 7, entry(EntityType.ZOMBIE, 3)),
                        pack(8, 5, 0, 0, 7, entry(EntityType.SKELETON, 2), entry(EntityType.SPIDER, 1)),
                        pack(10, 4, 80, 0, 7, entry(EntityType.HUSK, 2), entry(EntityType.STRAY, 1)),
                        pack(10, 3, 90, 0, 7, entry(EntityType.CREEPER, 1), entry(EntityType.ZOMBIE, 3)),
                        pack(10, 2, 100, 0, 7, entry(EntityType.ZOMBIE_VILLAGER, 1), entry(EntityType.ENDERMITE, 3)),
                        pack(12, 3, 100, 0, 7, entry(EntityType.ZOMBIE_VILLAGER, 2), entry(EntityType.BOGGED, 1)),
                        pack(12, 5, 120, 8, 14, entry(EntityType.CAVE_SPIDER, 2), entry(EntityType.DROWNED, 2)),
                        pack(14, 4, 130, 8, 14, entry(EntityType.WITCH, 1), entry(EntityType.BLAZE, 1), entry(EntityType.SLIME, 2)),
                        pack(16, 4, 150, 8, 14, entry(EntityType.GUARDIAN, 1), entry(EntityType.DROWNED, 2)),
                        pack(18, 3, 170, 8, 14, entry(EntityType.BREEZE, 1), entry(EntityType.MAGMA_CUBE, 2)),
                        pack(18, 5, 180, 15, Integer.MAX_VALUE, entry(EntityType.PILLAGER, 2), entry(EntityType.VINDICATOR, 1)),
                        pack(20, 4, 200, 15, Integer.MAX_VALUE, entry(EntityType.ENDERMAN, 1), entry(EntityType.WITHER_SKELETON, 1)),
                        pack(22, 4, 220, 15, Integer.MAX_VALUE, entry(EntityType.EVOKER, 1), entry(EntityType.PIGLIN_BRUTE, 1)),
                        pack(22, 3, 230, 15, Integer.MAX_VALUE, entry(EntityType.PIGLIN, 2), entry(EntityType.HOGLIN, 1)),
                        pack(24, 3, 240, 15, Integer.MAX_VALUE, entry(EntityType.RAVAGER, 1), entry(EntityType.ZOGLIN, 1)),
                        pack(24, 3, 260, 15, Integer.MAX_VALUE, entry(EntityType.GHAST, 1), entry(EntityType.SHULKER, 1)),
                        pack(26, 2, 280, 15, Integer.MAX_VALUE, entry(EntityType.VEX, 2), entry(EntityType.EVOKER, 1))
                ))
                .bossPacks(List.of(
                        bossPack(1, ironGolemBoss(220.0F, 16.0D, 6.0D)),
                        bossPack(2, ironGolemBoss(320.0F, 22.0D, 10.0D)),
                        bossPack(3, ironGolemBoss(460.0F, 30.0D, 14.0D))
                ))
                .build();
    }

    private static SpawnPack pack(int value, int weight, int minValue, int minStageIndex, int maxStageIndex, SpawnEntry... entries) {
        return SpawnPack.builder()
                .entries(List.of(entries))
                .value(value)
                .weight(weight)
                .minValue(minValue)
                .minStageIndex(minStageIndex)
                .maxStageIndex(maxStageIndex)
                .build();
    }

    private static SpawnEntry entry(EntityType<?> entity, int count) {
        return SpawnEntry.builder()
                .entity(EntityType.getKey(entity))
                .count(count)
                .health(health(entity))
                .build();
    }

    private static float health(EntityType<?> entity) {
        if (entity == EntityType.ENDERMITE) return 12.0F;
        if (entity == EntityType.SPIDER) return 20.0F;
        if (entity == EntityType.SKELETON || entity == EntityType.STRAY) return 22.0F;
        if (entity == EntityType.CREEPER || entity == EntityType.BOGGED) return 24.0F;
        if (entity == EntityType.ZOMBIE_VILLAGER) return 26.0F;
        if (entity == EntityType.HUSK) return 28.0F;
        if (entity == EntityType.CAVE_SPIDER) return 24.0F;
        if (entity == EntityType.SLIME || entity == EntityType.MAGMA_CUBE) return 30.0F;
        if (entity == EntityType.DROWNED || entity == EntityType.BREEZE) return 34.0F;
        if (entity == EntityType.BLAZE) return 36.0F;
        if (entity == EntityType.WITCH) return 40.0F;
        if (entity == EntityType.GUARDIAN) return 42.0F;
        if (entity == EntityType.PILLAGER) return 48.0F;
        if (entity == EntityType.ZOMBIFIED_PIGLIN) return 52.0F;
        if (entity == EntityType.PIGLIN) return 54.0F;
        if (entity == EntityType.VINDICATOR) return 58.0F;
        if (entity == EntityType.WITHER_SKELETON || entity == EntityType.SHULKER) return 64.0F;
        if (entity == EntityType.EVOKER) return 68.0F;
        if (entity == EntityType.ENDERMAN || entity == EntityType.GHAST) return 70.0F;
        if (entity == EntityType.PIGLIN_BRUTE) return 72.0F;
        if (entity == EntityType.HOGLIN) return 82.0F;
        if (entity == EntityType.ZOGLIN) return 88.0F;
        if (entity == EntityType.RAVAGER) return 110.0F;
        if (entity == EntityType.VEX) return 34.0F;
        return 24.0F;
    }

    private static BossSpawnPack bossPack(int order, BossSpawnEntry... entries) {
        return BossSpawnPack.builder()
                .order(order)
                .entries(List.of(entries))
                .build();
    }

    private static BossSpawnEntry ironGolemBoss(float health, double attackDamage, double armor) {
        return BossSpawnEntry.builder()
                .entity(EntityType.getKey(EntityType.IRON_GOLEM))
                .count(1)
                .health(health)
                .attackDamage(attackDamage)
                .armor(armor)
                .build();
    }
}
