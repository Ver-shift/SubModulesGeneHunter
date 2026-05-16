package org.galaxy.beyond.api.system.structure;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;
import net.minecraft.core.BlockPos;

/**
 * 安全区结构数据
 */
@Data
public class SafeZoneStructureData implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private int initialized; //多次init，1：找到安全区。2：按下传送锚点。
    @DescSynced
    @Persisted
    private BlockPos spawnPos = BlockPos.ZERO;
    @DescSynced
    @Persisted
    private BlockPos centerPos = BlockPos.ZERO;
}
