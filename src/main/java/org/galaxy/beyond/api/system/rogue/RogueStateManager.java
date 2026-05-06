package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.IRogueStateManager;
import org.galaxy.beyond.api.system.rogue.core.RogueState;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

public class RogueStateManager implements IRogueStateManager {
    @Override
    public void tick(ServerLevel level) {



        //全部玩家都要有的状态
        switch (isPlayerAllState(level)){
            case EMPTY -> {}
        }

        //只要有一个玩家达到了这种状态。比如某个玩家选择了

        //根据状态分配不同的任务
        switch (getRogueState(level)){
            case EMPTY -> {}
        }
    }

    @Override
    public PlayerRogueState isPlayerAllState(ServerLevel level) {
        return null;
    }

    @Override
    public boolean hasPlayerRogueState(ServerLevel level, PlayerRogueState state) {
        return false;
    }

    private RogueState getRogueState(ServerLevel level) {
        return BeyondAPI.getBeyondLevelData(level).getRogueData().getRogueState();
    }
}
