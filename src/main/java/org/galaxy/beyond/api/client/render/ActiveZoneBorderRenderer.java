package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.galaxy.beyond.api.system.zone.ZoneType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class ActiveZoneBorderRenderer {

    private static final int R = 220, G = 220, B = 220;
    private static final float ALPHA = 0.35F;

    private GpuBuffer vertexBuffer;
    private RenderSystem.AutoStorageIndexBuffer indices;
    private boolean needsRebuild = true;
    private double lastMinX, lastMinZ, lastMaxX, lastMaxZ;

    private RenderSystem.AutoStorageIndexBuffer getIndices() {
        if (indices == null) {
            indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        }
        return indices;
    }

    public void render(Level level, Vec3 cameraPos, PoseStack poseStack) {
        if (level == null) return;

        var dimData = BeyondAPI.getBeyondDimensionData(level);
        if (dimData == null) return;
        LevelZoneData zd = dimData.getLevelZoneData();
        if (zd == null) return;

        Set<Map.Entry<ChunkPos, ZoneType>> zones = zd.getZoneEntries();
        if (!zd.hasZones()) return;

        var active = ZoneHelper.filterByMask(zones, ZoneType.Active_Zone.mask());
        if (active.isEmpty()) return;
        ZoneHelper.Bounds b = active.bounds();

        double bx1 = b.minX() * 16.0, bx2 = (b.maxX() + 1) * 16.0;
        double bz1 = b.minZ() * 16.0, bz2 = (b.maxZ() + 1) * 16.0;
        float halfHeight = (float) (level.getMaxY() - level.getMinY()) * 0.5F;

        if (needsRebuild || bx1 != lastMinX || bz1 != lastMinZ || bx2 != lastMaxX || bz2 != lastMaxZ) {
            rebuildBuffer(bx1, bz1, bx2, bz2, halfHeight);
            lastMinX = bx1;
            lastMinZ = bz1;
            lastMaxX = bx2;
            lastMaxZ = bz2;
            needsRebuild = false;
        }

        float red = R / 255.0F, green = G / 255.0F, blue = B / 255.0F;
        float offset = (float) (System.currentTimeMillis() % 3000L) / 3000.0F;

        var ctx = RenderHelper.captureRenderContext();
        var indices = getIndices();
        GpuBuffer indexBuffer = indices.getBuffer(24);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(red, green, blue, ALPHA),
                        new Vector3f((float) (lastMinX - cameraPos.x), (float) -cameraPos.y, (float) (lastMinZ - cameraPos.z)),
                        new Matrix4f().translation(offset, offset, 0.0F)
                );

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Active zone border", ctx.colorTarget(), OptionalInt.empty(), ctx.depthTarget(), OptionalDouble.empty())) {
            RenderHelper.bindPassState(renderPass, ctx, dynamicTransforms, indexBuffer, indices, this.vertexBuffer);

            List<RenderPass.Draw<ActiveZoneBorderRenderer>> draws = new ArrayList<>(4);
            for (int side = 0; side < 4; side++) {
                draws.add(new RenderPass.Draw<>(0, this.vertexBuffer, indexBuffer, indices.type(), 6 * side, 6, 0));
            }
            renderPass.drawMultipleIndexed(draws, null, null, Collections.emptyList(), this);
        }
    }

    private void rebuildBuffer(double minX, double minZ, double maxX, double maxZ, float halfHeight) {
        float width = (float) (maxX - minX);
        float depth = (float) (maxZ - minZ);
        int vertexSize = DefaultVertexFormat.POSITION_TEX.getVertexSize();
        int vertexCount = 16;

        if (this.vertexBuffer == null) {
            this.vertexBuffer = RenderSystem.getDevice()
                    .createBuffer(() -> "Active zone border vbo",
                            GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                            (long) vertexCount * vertexSize);
        }

        try (ByteBufferBuilder byteBuf = ByteBufferBuilder.exactlySized(vertexCount * vertexSize)) {
            BufferBuilder builder = new BufferBuilder(byteBuf, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            RenderHelper.writeBoxWalls(builder, width, depth, halfHeight);
            try (MeshData meshData = builder.buildOrThrow()) {
                RenderSystem.getDevice().createCommandEncoder()
                        .writeToBuffer(this.vertexBuffer.slice(), meshData.vertexBuffer());
            }
        }
    }

    public void invalidate() {
        this.needsRebuild = true;
    }
}
