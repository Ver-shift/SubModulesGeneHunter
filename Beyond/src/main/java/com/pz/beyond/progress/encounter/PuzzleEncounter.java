package com.pz.beyond.progress.encounter;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.progress.SceneType;

/**
 * 有趣的事件或解密（绿色 + 资源类型）
 */
public class PuzzleEncounter extends EncounterType {
    public PuzzleEncounter() {
        super(SceneType.HARVEST);
    }
}
