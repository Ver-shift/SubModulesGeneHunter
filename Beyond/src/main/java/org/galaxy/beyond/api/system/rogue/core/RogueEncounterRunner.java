package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.event.custom.RogueEncounterEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.*;

/**
 * 节点遭遇运行器 —— 管理单个节点的遭遇解析 → 事件步进 → 解锁全流程。
 */
public class RogueEncounterRunner {

    private final ServerLevel level;
    private final IRogueContext ctx;
    private final RogueNodeData nodeData;
    private final EncounterBroadcaster broadcaster = new EncounterBroadcaster();
    private final SpawnSessionResolver spawnSessionResolver = new SpawnSessionResolver();

    public RogueEncounterRunner(ServerLevel level, IRogueContext ctx, RogueNodeData nodeData) {
        this.level = level;
        this.ctx = ctx;
        this.nodeData = nodeData;
    }

    // ============================================================
    // LOCKED → PRE_NODE
    // ============================================================

    public void handleLocked(ServerPlayer player) {
        nodeData.prepareForEncounter(level);
        ctx.getRogueData(level).setCurrentSpawn(null);
        BeyondAPI.syncLargeLevelData(level);
        BeyondAPI.syncGlobalData(level);
        ctx.setPlayerPhase(player, PlayerPhase.PRE_NODE);
        player.sendSystemMessage(Component.translatable("beyond.node.locked_triggered"));
    }

    // ============================================================
    // PRE_NODE → ON_EVENT（遭遇解析）
    // ============================================================

    public boolean tryResolveAndAdvance() {
        if (nodeData.getNodePhase() != NodePhase.PRE_NODE) return false;
        if (countPreNode() < ctx.playersInRogue(level).size()) return false;

        EncounterType encType = resolveEncounter();
        if (encType == null) return false;

        EventTask task = BeyondAPI.resolveEvent(level, encType);
        EncounterData encData = new EncounterData();
        encData.setType(encType);
        encData.setEvents(task);
        nodeData.startEncounter(level, encData);
        BeyondAPI.syncLargeLevelData(level);
        BeyondAPI.syncGlobalData(level);
        ctx.setAllPlayerPhase(level, PlayerPhase.ON_EVENT);

        broadcaster.encounterStart(level, encType);
        broadcastCurrentEvent();

        if (RogueEncounterEvent.post(new RogueEncounterEvent.Start(
                level, encType, task, ctx)).isCanceled())
            return false;

        castCurrentEvent(encType);
        return true;
    }

    // ============================================================
    // PRE_EVENT → ON_EVENT
    // ============================================================

    public boolean tryReadyPreEvent() {
        if (countPreEvent() < ctx.playersInRogue(level).size()) return false;
        nodeData.setNodePhase(level, NodePhase.ON_EVENT);
        BeyondAPI.syncLargeLevelData(level);
        BeyondAPI.syncGlobalData(level);
        ctx.setAllPlayerPhase(level, PlayerPhase.ON_EVENT);
        return true;
    }

    // ============================================================
    // ON_EVENT → 步进 / 解锁
    // ============================================================

    /**
     * 推进到下一个事件或解锁。返回 true 表示节点已结束。
     */
    public boolean advanceEvent(ServerPlayer player) {
        if (!nodeData.hasEncounter()) return false;
        if (!isCurrentEventCleared()) {
            player.sendSystemMessage(Component.translatable("beyond.event.not_cleared"));
            return false;
        }
        return advanceClearedEvent(player);
    }

    public boolean tryAutoAdvance() {
        if (nodeData.getNodePhase() != NodePhase.ON_EVENT) return false;
        if (!nodeData.hasEncounter()) return false;

        RogueEventType event = currentEvent();
        if (event == null) return false;

        int interval = event.auto();
        if (interval <= 0 || level.getGameTime() % interval != 0) return false;
        if (event.next(runnerContext(nodeData.getEncounterData().getType())) != RogueEventType.Result.SUCCESS)
            return false;

        return advanceClearedEvent(null);
    }

    private boolean advanceClearedEvent(ServerPlayer player) {
        var encData = nodeData.getEncounterData();
        int curIdx = nodeData.getCurrentEventIndex();
        // 最后一个事件 → 解锁
        if (curIdx >= nodeData.eventCount() - 1) {
            forceUnlockNode();
            return true;
        }

        int eventCount = nodeData.eventCount();
        boolean isLast = nodeData.advanceToNextEvent(level);
        BeyondAPI.syncLargeLevelData(level);
        BeyondAPI.syncGlobalData(level);

        broadcaster.currentEvent(level, encData.getEvents().getEvents(), nodeData.getCurrentEventIndex(), eventCount);

        if (RogueEncounterEvent.post(new RogueEncounterEvent.EventComplete(
                level, encData.getType(), encData.getEvents(), ctx,
                nodeData.getCurrentEventIndex(), eventCount)).isCanceled()) return false;

        if (isLast) {
            ctx.setAllPlayerPhase(level, PlayerPhase.ON_EVENT);
            castCurrentEvent(encData.getType());
        } else {
            if (player != null) {
                ctx.setPlayerPhase(player, PlayerPhase.PRE_EVENT);
            } else {
                ctx.setAllPlayerPhase(level, PlayerPhase.PRE_EVENT);
            }
            broadcaster.nextEvent(level);
            castCurrentEvent(encData.getType());
        }
        return false;
    }

    private boolean isCurrentEventCleared() {
        RogueEventType event = currentEvent();
        if (event == null) return true;
        return event.next(runnerContext(nodeData.getEncounterData().getType())) == RogueEventType.Result.SUCCESS;
    }

    private RogueEventType currentEvent() {
        var encData = nodeData.getEncounterData();
        if (encData == null || encData.getEvents() == null) return null;
        var instances = encData.getEvents().getEventInstances();
        int idx = nodeData.getCurrentEventIndex();
        return idx < instances.size() ? instances.get(idx) : null;
    }

    // ============================================================
    // 解锁 / 通关
    // ============================================================

    public void forceUnlockNode() {
        var encData = nodeData.getEncounterData();
        nodeData.markUnlocked(level);

        var rogueData = ctx.getRogueData(level);
        boolean allDone = rogueData.advanceProgress();
        BeyondAPI.syncLargeLevelData(level);
        BeyondAPI.syncGlobalData(level);

        var progressType = rogueData.getProgressType();
        int current = progressType != null ? progressType.getScenesIndex() : 0;
        int totalScenes = progressType != null ? progressType.getScenes().size() : 0;
        broadcaster.nodeUnlocked(level, current, totalScenes);

        BeyondAPI.zoneManager().addActiveZone(level, nodeData);
        BeyondAPI.syncGlobalData(level);

        if (encData != null && encData.getEvents() != null && encData.getEvents().hasEvents()) {
            RogueEncounterEvent.post(new RogueEncounterEvent.Complete(
                    level, encData.getType(), encData.getEvents(), ctx));
        }

        if (allDone) {
            ctx.setPhase(level, RoguePhase.PROGRESS_FINISH);
        } else {
            ctx.setPhase(level, RoguePhase.ON_PROGRESS);
            ctx.setAllPlayerPhase(level, PlayerPhase.ON_PROGRESS);
        }
    }

    // ============================================================
    // 遭遇解析
    // ============================================================

    private EncounterType resolveEncounter() {
        var rogueData = ctx.getRogueData(level);
        var encData = nodeData.getEncounterData();
        if (encData != null && encData.getType() != null) {
            return encData.getType();
        }
        var pt = rogueData.getProgressType();
        if (pt == null || pt.getScenes().isEmpty()) return null;
        int idx = pt.getClampedScenesIndex();
        var et = EncounterType.from(nodeData.getNodeData().getColor(), pt.getScenes().get(idx));
        if (et != null && encData != null) {
            encData.setType(et);
        }
        return et;
    }

    // ============================================================
    // 广播 + 计数
    // ============================================================

    private void broadcastCurrentEvent() {
        if (!nodeData.hasEncounter()) return;
        var ids = nodeData.getEncounterData().getEvents().getEvents();
        int total = nodeData.eventCount();
        int idx = nodeData.getCurrentEventIndex();
        if (idx < ids.size()) {
            broadcaster.currentEvent(level, ids, idx, total);
        }
    }

    private int countPreNode() {
        int c = 0;
        for (var p : ctx.playersInRogue(level))
            if (ctx.getPlayerPhase(p) == PlayerPhase.PRE_NODE) c++;
        return c;
    }

    private int countPreEvent() {
        int c = 0;
        for (var p : ctx.playersInRogue(level))
            if (ctx.getPlayerPhase(p) == PlayerPhase.PRE_EVENT) c++;
        return c;
    }

    public boolean isPreNode() {
        return nodeData.getNodePhase() == NodePhase.PRE_NODE;
    }

    public boolean isPreEvent() {
        return nodeData.getNodePhase() == NodePhase.PRE_EVENT;
    }

    public boolean isOnEvent() {
        return nodeData.getNodePhase() == NodePhase.ON_EVENT;
    }

    /**
     * 对当前事件的 RogueEventType 执行 cast
     */
    private void castCurrentEvent(EncounterType encType) {
        var instances = nodeData.getEncounterData().getEvents().getEventInstances();
        int idx = nodeData.getCurrentEventIndex();
        if (idx < instances.size()) {
            instances.get(idx).cast(runnerContext(encType));
        }
    }

    private RogueEventType.Context runnerContext(EncounterType encType) {
        return new RogueEventType.Context(encType, level, nodePos(), ctx,
                spawnSessionResolver.resolve(level, ctx, nodeData, encType, currentEvent()));
    }

    private BlockPos nodePos() {
        BlockPos pos = nodeData.getNodePos();
        if (pos != null && !pos.equals(BlockPos.ZERO)) return pos;
        return nodeData.getNodeChunk().getWorldPosition();
    }
}
