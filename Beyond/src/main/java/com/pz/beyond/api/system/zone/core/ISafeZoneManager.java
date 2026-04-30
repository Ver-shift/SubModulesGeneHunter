package com.pz.beyond.api.system.zone.core;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.level.Level;

/**
 * 安全区管理器接口：聚合与当前维度安全区相关的客户端行为。
 * <p>
 * 服务端的 chunk 归属由 {@link IZonePosManager#addSafeZone} 负责维护，
 * 本接口目前只承担客户端的可视化职责。
 */
public interface ISafeZoneManager {

    /**
     * 渲染当前维度 SafeZone 的外部边界。
     * <p>
     * 实现方应从 {@link com.pz.beyond.api.system.zone.LevelZoneData#getZonePos()}
     * 中收集所有标记为 {@code SAFE_ZONE} 的 chunk，取其最小外接矩形作为边界。
     *
     * @param level     当前客户端世界
     * @param poseStack 渲染矩阵栈（来自 {@code RenderLevelStageEvent}）
     * @param camera    主相机
     */
    void renderBorder(Level level, PoseStack poseStack, Camera camera);

}
