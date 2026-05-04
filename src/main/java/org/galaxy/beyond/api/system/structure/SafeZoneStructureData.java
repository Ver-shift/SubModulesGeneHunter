package org.galaxy.beyond.api.system.structure;

import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/**
 * 安全区结构数据
 */
@Data
public class SafeZoneStructureData {
    private int initialized = 0; //多次init，1：找到安全区。2：按下传送锚点。
    private BlockPos spawnPos =  BlockPos.ZERO;
    private BlockPos centerPos = BlockPos.ZERO;

}
