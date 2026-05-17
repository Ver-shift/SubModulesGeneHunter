package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.BeyondAPI;

import java.util.*;

/**
 * 进度管理器 —— 读取 ProgressType.scenes 作为进度条，管理进度推进与遭遇类型生成。
 */
public class ProgressManager {

    private RogueData getData(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData();
    }

    public int getCurrentProgressIndex(ServerLevel level) {
        return getData(level).getProgressIndex();
    }

    public int getTotalProgress(ServerLevel level) {
        ProgressType pt = getData(level).getProgressType();
        return pt != null ? pt.getScenes().size() : 0;
    }

    public SceneType getSceneType(ServerLevel level, int index) {
        ProgressType pt = getData(level).getProgressType();
        if (pt != null && index >= 0 && index < pt.getScenes().size()) {
            return pt.getScenes().get(index);
        }
        // 无 ProgressType 时的默认映射
        int total = getTotalProgress(level);
        if (total == 0) total = 10;
        if (index == total - 1) return SceneType.CLIMAX;
        if (index == total / 3) return SceneType.REPOSE;
        return SceneType.HARVEST;
    }

    public void advanceProgress(ServerLevel level) {
        var data = getData(level);
        data.setProgressIndex(data.getProgressIndex() + 1);
    }

    /** 为待分配区块生成遭遇类型 */
    public void generateEncounterTypes(ServerLevel level, Collection<ChunkPos> pendingChunks, long seed) {
        Random random = new Random(seed);
        var data = getData(level);
        SceneType currentScene = getSceneType(level, data.getProgressIndex());
        EncounterType[] pool = EncounterType.byScene(currentScene).toArray(EncounterType[]::new);
        if (pool.length == 0) return;

        for (ChunkPos chunk : pendingChunks) {
            data.getEncounterAssignments().put(chunk, pool[random.nextInt(pool.length)]);
        }
    }

    public EncounterType getEncounterType(ServerLevel level, ChunkPos chunk) {
        return getData(level).getEncounterAssignments().get(chunk);
    }
}
