package org.galaxy.beyond.api.system.spawn;

import net.minecraft.util.RandomSource;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.List;

/**
 * 单次刷怪计算的输入。
 */
public record SpawnContext(int stageIndex, NodeColor nodeColor, RandomSource random, List<SceneType> scenes) {

    public SpawnContext(int stageIndex, NodeColor nodeColor, RandomSource random) {
        this(stageIndex, nodeColor, random, List.of());
    }

    public SpawnContext {
        nodeColor = nodeColor == null ? NodeColor.EMPTY : nodeColor;
        if (random == null) {
            throw new IllegalArgumentException("random cannot be null");
        }
        scenes = scenes == null ? List.of() : List.copyOf(scenes);
    }
}
