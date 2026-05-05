package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.IPlayerRougeManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueStateManager;
import org.galaxy.beyond.api.system.rogue.player.PlayerRogueState;

public class RogueStateManager implements IRogueStateManager {
    @Override
    public void tick(ServerLevel level) {
        switch (isPlayerAllState(level)){
            case IN_SAFE_ZONE -> {}
            case READY_ROGUE -> {}
            case ON_ROGUE -> {}
            case IN_NODE -> {}
            case READY_NODE -> {}
            case IN_NODE_EVENT -> {}
            case READY_NEXT -> {}
            case DEAD -> {}
            case SPECTATING -> {}
            case EMPTY -> {}
        }

        switch (getRogueState(level)){
            case LOBBY ->{}
            case READY ->{}
            case IN_PROGRESS ->{}
            case IN_NODE ->{}
            case POST_GAME->{}
            case EMPTY->{}
        }
    }

    @Override
    public PlayerRogueState isPlayerAllState(ServerLevel level) {
        return null;
    }

    private RogueState getRogueState(ServerLevel level) {
        return BeyondAPI.getBeyondLevelData(level).getRogueData().getRogueState();
    }
}
