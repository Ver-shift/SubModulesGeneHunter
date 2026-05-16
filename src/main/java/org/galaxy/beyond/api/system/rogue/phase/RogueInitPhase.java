package org.galaxy.beyond.api.system.rogue.phase;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.IRoguePhase;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.Map;

public class RogueInitPhase implements IRoguePhase {

    @Override
    public void enter(ServerLevel level, RogueContext ctx) {
        long seed = ctx.getGameSeed(level);

        // 初始化活动区域
        var zoneManager = BeyondAPI.getBeyondManager().getZoneManager();
        zoneManager.activeZoneInit(level);

        // 为所有节点区块生成遭遇类型
        var zoneEntries = BeyondAPI.getBeyondDimensionData(level).getLevelZoneData().getZoneEntries();
        var nodeChunks = zoneEntries.stream()
                .filter(e -> e.getValue() == ZoneType.Node_Zone)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        ctx.getProgressManager().generateEncounterTypes(level, nodeChunks, seed);

        ctx.getSceneManager().nextScene(level);
    }
}
