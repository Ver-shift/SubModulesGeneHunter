package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;

public class LobbyPhase implements IRoguePhase {

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        // 安全区等待，玩家自由活动
    }
}
