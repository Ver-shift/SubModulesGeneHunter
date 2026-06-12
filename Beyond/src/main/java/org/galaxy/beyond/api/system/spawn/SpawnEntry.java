package org.galaxy.beyond.api.system.spawn;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;

/**
 * 生成包里的单个实体条目。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpawnEntry implements IPersistedSerializable {

    @Persisted
    private ResourceLocation entity;

    @Persisted
    @Builder.Default
    private int count = 1;
}
