package org.galaxy.gene_hunter.data.spawn;

import net.minecraft.world.entity.EntityType;
import org.galaxy.beyond.api.system.spawn.SpawnEntry;

public final class GeneHunterSpawnEntries {

    private GeneHunterSpawnEntries() {
    }

    public static SpawnEntry entry(EntityType<?> entity, int count) {
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
}
