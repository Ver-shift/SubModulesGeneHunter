package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;

public class OnProgressPhase implements IRoguePhase {

    @Override
    public void tick(ServerLevel level, RogueContext ctx) {
        // 检测玩家是否在节点区域内触发了节点方块
        // 如果有任何一个玩家的状态变成 PRE_NODE，条件就会触发流转
    }
}
