package com.pz.beyond.api.system.progress.core;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.node.NodeData;
import com.pz.beyond.api.system.node.NodeState;
import com.pz.beyond.api.system.progress.ProgressData;
import com.pz.beyond.api.system.progress.ProgressManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/**
 * tick 驱动的关卡会话管理器。
 * <p>
 * 管理关卡宏观生命周期：INACTIVE → WARMUP → IN_PROGRESS → COMPLETED。
 * 由 {@link ProgressManager#tick(ServerLevel)} 每 tick 调用。
 */
public class ProgressSession {

    // === 状态 ===
    private SessionState sessionState = SessionState.INACTIVE;

    // === 计时器（单位：tick）===
    private int sessionTimer = 0;

    // === 配置（tick 单位）===
    private int warmUpTicks = 200; // 热身 10 秒

    // === 场景进度 ===
    private int currentSceneIndex = 0;
    private int totalScenes = 0;

    // === 关联 ===
    private final ProgressManager progressManager;
    private ProgressData progressData;

    public ProgressSession(ProgressManager progressManager) {
        this.progressManager = progressManager;
    }

    // ==================== 公开 API ====================

    /** 每 tick 由 ProgressManager.tick() 调用 */
    public void tick(ServerLevel level) {
        if (progressData == null) {
            return;
        }

        switch (sessionState) {
            case INACTIVE, COMPLETED -> { /* 不处理 */ }

            case WARMUP -> {
                sessionTimer--;
                if (sessionTimer <= 0) {
                    transitionTo(SessionState.IN_PROGRESS, level);
                }
            }

            case IN_PROGRESS -> {
                if (isCurrentSceneComplete()) {
                    if (currentSceneIndex + 1 >= totalScenes) {
                        transitionTo(SessionState.COMPLETED, level);
                    } else {
                        currentSceneIndex++;
                        progressData.setIndex(currentSceneIndex);
                        Beyond.debugLog("[ProgressSession] scene advanced: index={}/{}", currentSceneIndex, totalScenes);
                    }
                }
            }
        }
    }

    /**
     * 由战利品袋右键调用，从 INACTIVE 启动游戏。
     *
     * @return true 表示启动成功，false 表示当前状态不允许启动
     */
    public boolean startSession(ResourceLocation progressId, ServerLevel level) {
        if (this.sessionState != SessionState.INACTIVE) {
            return false;
        }

        // 委托 ProgressManager 初始化关卡数据
        progressManager.initProgress(progressId);

        // 延迟获取 ProgressData
        BeyondLevelData levelData = BeyondAPI.getBeyondLevelData(level);
        if (levelData == null) {
            Beyond.debugLog("[ProgressSession] startSession failed: levelData is null");
            return false;
        }
        this.progressData = levelData.getProgressData();
        this.totalScenes = progressData.getSceneTypes().size();
        this.currentSceneIndex = 0;

        transitionTo(SessionState.WARMUP, level);
        return true;
    }

    public boolean canInteractWithNode() {
        return sessionState == SessionState.IN_PROGRESS;
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    public int getCurrentSceneIndex() {
        return currentSceneIndex;
    }

    public int getTotalScenes() {
        return totalScenes;
    }

    public int getSessionTimer() {
        return sessionTimer;
    }

    // ==================== 内部方法 ====================

    private void transitionTo(SessionState target, ServerLevel level) {
        SessionState old = this.sessionState;
        this.sessionState = target;
        Beyond.debugLog("[ProgressSession] transition: {} -> {}", old.getSerializedName(), target.getSerializedName());

        switch (target) {
            case WARMUP -> {
                this.sessionTimer = this.warmUpTicks;
            }
            case IN_PROGRESS -> {
                // 广播游戏正式开始
                for (ServerPlayer player : level.players()) {
                    player.displayClientMessage(Component.translatable("beyond.game.start"), true);
                }
            }
            case COMPLETED -> {
                // 广播关卡完成
                for (ServerPlayer player : level.players()) {
                    player.displayClientMessage(Component.translatable("beyond.game.complete"), false);
                }
            }
        }
    }

    /** 检查当前场景的所有节点是否都已完成 */
    private boolean isCurrentSceneComplete() {
        if (progressData == null) {
            return false;
        }
        Map<com.pz.beyond.api.system.node.StructureKey, NodeData> nodes = progressData.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            return false;
        }
        for (NodeData node : nodes.values()) {
            if (node.getNodeState() != NodeState.COMPLETED) {
                return false;
            }
        }
        return true;
    }
}
