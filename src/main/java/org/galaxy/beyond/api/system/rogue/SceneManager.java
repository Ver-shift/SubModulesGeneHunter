package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.ISceneManager;

public class SceneManager implements ISceneManager {

    @Override
    public void nextScene(ServerLevel level) {
        var progressType = BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData().getProgressType();
        if (progressType != null && !progressType.getScenes().isEmpty()) {
            int next = progressType.getScenesIndex() + 1;
            if (next < progressType.getScenes().size()) {
                progressType.setScenesIndex(next);
            }
        }
    }
}
