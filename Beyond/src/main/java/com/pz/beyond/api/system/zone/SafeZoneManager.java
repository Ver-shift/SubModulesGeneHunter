package com.pz.beyond.api.system.zone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.zone.core.ISafeZoneManager;
import com.pz.beyond.api.util.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;

/**
 * {@link ISafeZoneManager} 的默认实现，负责 SafeZone 的客户端可视化。
 * <p>
 * 算法：遍历 {@link LevelZoneData#getZonePos()} 中 type 为 {@code SAFE_ZONE} 的 chunk，
 * 取它们的最小外接矩形（axis-aligned bounding rectangle）作为渲染边界。
 * 由于 {@link ZonePosManager#addSafeZone} 铺设的就是方形区域，因此外接矩形即为安全区本身。
 */
public class SafeZoneManager implements ISafeZoneManager {

    /** 复用原版世界边界的力场纹理，保证 UV 动画与边界墙效果一致。 */
    private static final ResourceLocation BORDER_TEX =
            ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    /** 绿色调 —— SafeZone 视觉上要显得"友好"。 */
    private static final int COLOR_R = 64;
    private static final int COLOR_G = 220;
    private static final int COLOR_B = 120;

    /** 底部较实、顶部较淡，营造屏障向上衰减的观感。 */
    private static final int BOTTOM_ALPHA = 180;
    private static final int TOP_ALPHA = 40;

    @Override
    public void renderBorder(Level level, PoseStack poseStack, Camera camera) {
        if (level == null || poseStack == null || camera == null) {
            return;
        }
        BeyondLevelData beyondLevelData = BeyondAPI.getBeyondLevelData(level);
        if (beyondLevelData == null) {
            return;
        }
        LevelZoneData zoneData = beyondLevelData.getLevelZoneData();
        if (zoneData == null || zoneData.getZonePos() == null || zoneData.getZonePos().isEmpty()) {
            return;
        }

        ZoneType safeZone = BeyondZoneInit.SAFE_ZONE.get();
        if (safeZone == null) {
            return;
        }

        int minChunkX = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;
        boolean hasAny = false;

        for (Map.Entry<Long, ZoneType> entry : zoneData.getZonePos().entrySet()) {
            if (entry.getValue() != safeZone) {
                continue;
            }
            ChunkPos cp = new ChunkPos(entry.getKey());
            if (cp.x < minChunkX) minChunkX = cp.x;
            if (cp.x > maxChunkX) maxChunkX = cp.x;
            if (cp.z < minChunkZ) minChunkZ = cp.z;
            if (cp.z > maxChunkZ) maxChunkZ = cp.z;
            hasAny = true;
        }
        if (!hasAny) {
            return;
        }

        // chunk 坐标转换为方块坐标：最大侧 +1 覆盖整块的右/下边界。
        double minX = minChunkX * 16.0;
        double maxX = (maxChunkX + 1) * 16.0;
        double minZ = minChunkZ * 16.0;
        double maxZ = (maxChunkZ + 1) * 16.0;

        RenderUtil.renderBorder(
                minX, minZ, maxX, maxZ,
                COLOR_R, COLOR_G, COLOR_B,
                BOTTOM_ALPHA, TOP_ALPHA,
                BORDER_TEX,
                camera, poseStack
        );
    }
}
