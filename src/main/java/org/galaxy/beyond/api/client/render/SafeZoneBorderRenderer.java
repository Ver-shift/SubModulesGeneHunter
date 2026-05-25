package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

/**
 * 安全区边框 —— 单矩形，颜色随玩家 Phase 变化（蓝→橙→红）。
 */
public class SafeZoneBorderRenderer extends ZoneBorderRenderer {

    private static final float ALPHA = 0.65F;
    private static final int R_BLUE = 64, G_BLUE = 120, B_BLUE = 220;
    private static final int R_ORANGE = 220, G_ORANGE = 140, B_ORANGE = 30;
    private static final int R_RED = 200, G_RED = 30, B_RED = 30;

    private double lastMinX, lastMinZ, lastMaxX, lastMaxZ;
    private int frameCounter;
    private int curR = R_BLUE, curG = G_BLUE, curB = B_BLUE;
    private int tgtR = R_BLUE, tgtG = G_BLUE, tgtB = B_BLUE;

    public void render(Level level, Vec3 cameraPos, PoseStack ps) {
        if (level == null) return;
        LevelZoneData lzd = BeyondAPI.getLevelZoneData(level);
        if (lzd == null || !lzd.hasZones()) return;
//        WorldBorderRenderer
        Set<ChunkPos> safeSet = lzd.safeChunks();
        if (safeSet.isEmpty()) return;
        ZoneHelper.Bounds b = ZoneHelper.boundsOf(safeSet);
        float halfHeight = (level.getMaxY() - level.getMinY()) * 0.5f;

        double bx1 = b.minX() * 16.0, bx2 = (b.maxX() + 1) * 16.0;
        double bz1 = b.minZ() * 16.0, bz2 = (b.maxZ() + 1) * 16.0;

        if (needsRebuild || bx1 != lastMinX || bz1 != lastMinZ || bx2 != lastMaxX || bz2 != lastMaxZ) {
            ensureBuffer(16);
            try (ByteBufferBuilder bb = ByteBufferBuilder.exactlySized(16 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bb, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                RenderHelper.writeBoxWalls(builder, (float) (bx2 - bx1), (float) (bz2 - bz1), halfHeight);
                try (MeshData md = builder.buildOrThrow()) {
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.vertexBuffer.slice(), md.vertexBuffer());
                }
            }
            indexCount = 24;
            lastMinX = bx1;
            lastMinZ = bz1;
            lastMaxX = bx2;
            lastMaxZ = bz2;
            needsRebuild = false;
        }

        frameCounter++;
        if (frameCounter % 20 == 0) updateColor();
        drawBorder("Safe zone", curR / 255f, curG / 255f, curB / 255f, ALPHA,
                lastMinX - cameraPos.x, -cameraPos.y, lastMinZ - cameraPos.z);
    }

    private void updateColor() {
        var player = Minecraft.getInstance().player;
        if (player == null) { tgtR=R_BLUE; tgtG=G_BLUE; tgtB=B_BLUE; return; }
        try {
            var phase = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
            if (phase == PlayerPhase.LOBBY)          { tgtR=R_BLUE; tgtG=G_BLUE; tgtB=B_BLUE; }
            else if (phase == PlayerPhase.PRE_ROGUE)  { tgtR=R_ORANGE; tgtG=G_ORANGE; tgtB=B_ORANGE; }
            else                                      { tgtR=R_RED; tgtG=G_RED; tgtB=B_RED; }
        } catch (Exception e) { tgtR=R_BLUE; tgtG=G_BLUE; tgtB=B_BLUE; }
        float rate = 0.1f;
        curR += (int)((tgtR - curR) * rate + 0.5f);
        curG += (int)((tgtG - curG) * rate + 0.5f);
        curB += (int)((tgtB - curB) * rate + 0.5f);
    }

    @Override protected void rebuildBuffer() {}
}
