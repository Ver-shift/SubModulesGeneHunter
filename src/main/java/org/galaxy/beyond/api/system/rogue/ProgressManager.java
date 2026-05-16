package org.galaxy.beyond.api.system.rogue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.ProgressEventType;

import java.util.*;

/**
 * 进度管理器 —— 后端。管理行动路线、进度推进、节点类型生成。
 */
public class ProgressManager {

    private final Map<String, ActionRoute> routes = new LinkedHashMap<>();
    private ActionRoute currentRoute;

    public void registerRoute(ActionRoute route) {
        routes.put(route.getId().toString(), route);
    }

    public ActionRoute getRoute(String id) {
        return routes.get(id);
    }

    public Collection<ActionRoute> getAllRoutes() {
        return routes.values();
    }

    public void setCurrentRoute(ActionRoute route) {
        this.currentRoute = route;
    }

    public ActionRoute getCurrentRoute() {
        return currentRoute;
    }

    public int getCurrentProgressIndex(ServerLevel level) {
        var data = BeyondAPI.getBeyondDimensionData(level).getRogueData();
        return data.getProgressIndex();
    }

    public int getTotalProgress(ServerLevel level) {
        if (currentRoute == null) return 10;
        return currentRoute.getTotalProgress();
    }

    public ProgressEventType getProgressEventType(ServerLevel level, int index) {
        if (currentRoute != null) {
            var step = currentRoute.getStep(index);
            if (step != null) return step.getEventType();
        }
        // 默认：最后一个是 Boss，1/3 处是 CORRECTION
        int total = getTotalProgress(level);
        if (index == total - 1) return ProgressEventType.BOSS;
        if (index == total / 3) return ProgressEventType.CORRECTION;
        return ProgressEventType.RESOURCE;
    }

    /** 推进进度 +1 */
    public void advanceProgress(ServerLevel level) {
        var data = BeyondAPI.getBeyondDimensionData(level).getRogueData();
        data.setProgressIndex(data.getProgressIndex() + 1);
    }

    private static final EncounterType[] HARVEST_ENCOUNTERS = {
        EncounterType.Green_Event, EncounterType.Orange_NormalMonster, EncounterType.Red_EliteMonster
    };
    private static final EncounterType[] REPOSE_ENCOUNTERS = {
        EncounterType.Green_Bonfire, EncounterType.Orange_NormalShop, EncounterType.Red_CursedShop
    };
    private static final EncounterType[] BOSS_ENCOUNTERS = {
        EncounterType.Green_BossShop, EncounterType.Orange_BossShop, EncounterType.Red_BossShop
    };

    public void generateEncounterTypes(ServerLevel level, Collection<ChunkPos> pendingChunks, long seed) {
        Random random = new Random(seed);
        var data = BeyondAPI.getBeyondDimensionData(level).getRogueData();
        int progressIndex = data.getProgressIndex();
        ProgressEventType eventType = getProgressEventType(level, progressIndex);

        EncounterType[] pool = switch (eventType) {
            case RESOURCE -> HARVEST_ENCOUNTERS;
            case CORRECTION -> REPOSE_ENCOUNTERS;
            case BOSS -> BOSS_ENCOUNTERS;
        };

        for (ChunkPos chunk : pendingChunks) {
            data.getEncounterAssignments().put(chunk, pool[random.nextInt(pool.length)]);
        }
    }

    public EncounterType getEncounterType(ServerLevel level, ChunkPos chunk) {
        return BeyondAPI.getBeyondDimensionData(level).getRogueData().getEncounterAssignments().get(chunk);
    }

    /** 给安全区创建默认行动路线 */
    public static ActionRoute createDefaultRoute() {
        ActionRoute route = new ActionRoute(
                org.galaxy.beyond.Beyond.asResource("default"), "默认路线");
        // 10 个进度：1-3 资源, 4 修正, 5-8 资源, 9 Boss
        route.addStep(ProgressEventType.RESOURCE);    // 0
        route.addStep(ProgressEventType.RESOURCE);    // 1
        route.addStep(ProgressEventType.RESOURCE);    // 2
        route.addStep(ProgressEventType.CORRECTION);  // 3 (1/3 处)
        route.addStep(ProgressEventType.RESOURCE);    // 4
        route.addStep(ProgressEventType.RESOURCE);    // 5
        route.addStep(ProgressEventType.RESOURCE);    // 6
        route.addStep(ProgressEventType.RESOURCE);    // 7
        route.addStep(ProgressEventType.RESOURCE);    // 8
        route.addStep(ProgressEventType.BOSS);        // 9 (最后)
        return route;
    }
}
