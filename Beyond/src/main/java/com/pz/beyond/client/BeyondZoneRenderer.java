package com.pz.beyond.client;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.config.ServerConfig;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.zone.LevelZoneData;
import com.pz.beyond.api.system.zone.ZoneType;
import com.pz.beyond.api.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 客户端区域渲染事件订阅器。
 * <p>
 * 渲染 SafeZone 外部边界 + debug 模式下的 NodeZone 边界。
 * NodeZone 采用连通分量算法：相邻区块合并为一个矩形，避免渲染大量碎片格子。
 */
@EventBusSubscriber(modid = Beyond.MODID, value = Dist.CLIENT)
public class BeyondZoneRenderer {

    private static final ResourceLocation BORDER_TEX =
            ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    /** 橙色 —— 节点区域用暖色区分 */
    private static final int NODE_COLOR_R = 255;
    private static final int NODE_COLOR_G = 140;
    private static final int NODE_COLOR_B = 0;
    private static final int NODE_BOTTOM_ALPHA = 140;
    private static final int NODE_TOP_ALPHA = 30;

    /** 只渲染玩家周围 N 个区块内的节点区域，防止全图遍历卡顿 */
    private static final int RENDER_CHUNK_RADIUS = 10;

    /**
     * 在半透明方块渲染之后绘制屏障，避免 alpha 混合覆盖方块像素。
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        if (!BeyondAttachInit.isAllowedDimension(level)) {
            return;
        }

        BeyondAPI.getBeyondManager()
                .getSafeZoneManager()
                .renderBorder(level, event.getPoseStack(), mc.gameRenderer.getMainCamera());

        // debug 模式下渲染节点区域
        if (ServerConfig.Generic.DEBUG_MESSAGES.get()) {
            renderNodeZones(level, event.getPoseStack(), mc.gameRenderer.getMainCamera());
        }
    }

    /**
     * 渲染所有 NODE_ZONE 区域边界。
     * <p>
     * 算法：收集所有 NODE_ZONE 区块 → 4邻域连通分量分组 → 每组取最小外接矩形渲染。
     * 这样多个邻近节点自动合并为最外层边界，不相邻的节点各自独立渲染。
     */
    private static void renderNodeZones(ClientLevel level, PoseStack poseStack, Camera camera) {
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        if (beyondLevelData == null) {
            return;
        }
        LevelZoneData zoneData = beyondLevelData.getLevelZoneData();
        if (zoneData == null || zoneData.getZonePos() == null || zoneData.getZonePos().isEmpty()) {
            return;
        }

        ZoneType nodeZone = BeyondZoneInit.NODE_ZONE.get();
        if (nodeZone == null) {
            return;
        }

        // 1) 获取玩家区块坐标，筛选周围 RENDER_CHUNK_RADIUS 内的 node zone 区块
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ChunkPos playerChunk = player.chunkPosition();
        int radius = RENDER_CHUNK_RADIUS;

        Set<Long> nodeChunks = new HashSet<>();
        for (Map.Entry<Long, ZoneType> entry : zoneData.getZonePos().entrySet()) {
            if (entry.getValue() != nodeZone) {
                continue;
            }
            ChunkPos cp = new ChunkPos(entry.getKey());
            // Chebyshev 距离：只保留玩家周围 radius 个区块内的节点
            if (Math.abs(cp.x - playerChunk.x) <= radius && Math.abs(cp.z - playerChunk.z) <= radius) {
                nodeChunks.add(entry.getKey());
            }
        }
        if (nodeChunks.isEmpty()) {
            return;
        }

        // 2) 连通分量分组：4邻域 flood-fill
        List<Set<Long>> clusters = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        for (Long key : nodeChunks) {
            if (visited.contains(key)) continue;
            Set<Long> cluster = new HashSet<>();
            floodFillCluster(key, nodeChunks, cluster);
            visited.addAll(cluster);
            clusters.add(cluster);
        }

        // 3) 每个 cluster 取外接矩形渲染
        for (Set<Long> cluster : clusters) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (Long key : cluster) {
                ChunkPos cp = new ChunkPos(key);
                if (cp.x < minX) minX = cp.x;
                if (cp.x > maxX) maxX = cp.x;
                if (cp.z < minZ) minZ = cp.z;
                if (cp.z > maxZ) maxZ = cp.z;
            }

            double blockMinX = minX * 16.0;
            double blockMaxX = (maxX + 1) * 16.0;
            double blockMinZ = minZ * 16.0;
            double blockMaxZ = (maxZ + 1) * 16.0;

            RenderUtil.renderBorder(
                    blockMinX, blockMinZ, blockMaxX, blockMaxZ,
                    NODE_COLOR_R, NODE_COLOR_G, NODE_COLOR_B,
                    NODE_BOTTOM_ALPHA, NODE_TOP_ALPHA,
                    BORDER_TEX,
                    camera, poseStack
            );
        }
    }

    /** 4邻域 flood fill —— 将 key 所在的连通簇填入 cluster */
    private static void floodFillCluster(Long key, Set<Long> allNodeChunks, Set<Long> cluster) {
        if (!allNodeChunks.contains(key) || !cluster.add(key)) {
            return;
        }
        ChunkPos cp = new ChunkPos(key);
        floodFillCluster(new ChunkPos(cp.x + 1, cp.z).toLong(), allNodeChunks, cluster);
        floodFillCluster(new ChunkPos(cp.x - 1, cp.z).toLong(), allNodeChunks, cluster);
        floodFillCluster(new ChunkPos(cp.x, cp.z + 1).toLong(), allNodeChunks, cluster);
        floodFillCluster(new ChunkPos(cp.x, cp.z - 1).toLong(), allNodeChunks, cluster);
    }
}
