package org.galaxy.beyond.api.system.spawn;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;

/**
 * Boss 怪物团里的固定实体条目。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossSpawnEntry implements IPersistedSerializable {

    @Persisted
    private ResourceLocation entity;

    @Persisted
    @Builder.Default
    private int count = 1;

    @Persisted
    @Builder.Default
    private float health = 20.0F;

    @Persisted
    private double attackDamage;

    @Persisted
    private double armor;
}
