package org.galaxy.beyond.api.system.spawn;

import net.minecraft.util.RandomSource;
import org.galaxy.beyond.api.system.node.NodeColor;

/**
 * 单次刷怪计算的输入。
 */
public record SpawnContext(int stageIndex, NodeColor nodeColor, RandomSource random) {

    public SpawnContext {
        nodeColor = nodeColor == null ? NodeColor.EMPTY : nodeColor;
        if (random == null) {
            throw new IllegalArgumentException("random cannot be null");
        }
    }
}
