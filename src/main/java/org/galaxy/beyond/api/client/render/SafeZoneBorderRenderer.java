package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneType;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class SafeZoneBorderRenderer implements ISafeZoneBorderRenderer {

    private static final Identifier FORCEFIELD =
            Identifier.withDefaultNamespace("textures/misc/forcefield.png");
    private static final int R = 64, G = 120, B = 220;
    private static final float ALPHA = 0.65F;

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
    @Override
    public void render(Level level, Vec3 cameraPos, PoseStack poseStack) {
        if (level == null) return;

        var dimData = BeyondAPI.getBeyondDimensionData(level);
        if (dimData == null) return;
        LevelZoneData zd = dimData.getLevelZoneData();
        if (zd == null) return;

        Set<Map.Entry<ChunkPos, ZoneType>> zones = zd.getZoneEntries();
        if (!zd.hasZones()) return;

        int minXC = Integer.MAX_VALUE, maxXC = Integer.MIN_VALUE;
        int minZC = Integer.MAX_VALUE, maxZC = Integer.MIN_VALUE;
        boolean found = false;

        for (var e : zones) {
            if (e.getValue() != ZoneType.Safe_Zone) continue;
            int cx = e.getKey().x(), cz = e.getKey().z();
            if (cx < minXC) minXC = cx;
            if (cx > maxXC) maxXC = cx;
            if (cz < minZC) minZC = cz;
            if (cz > maxZC) maxZC = cz;
            found = true;
        }
        if (!found) return;

        double bx1 = minXC * 16.0, bx2 = (maxXC + 1) * 16.0;
        double bz1 = minZC * 16.0, bz2 = (maxZC + 1) * 16.0;
        float halfHeight = (float)(level.getMaxY() - level.getMinY()) * 0.5F;

        if (needsRebuild || bx1 != lastMinX || bz1 != lastMinZ || bx2 != lastMaxX || bz2 != lastMaxZ) {
            rebuildBuffer(bx1, bz1, bx2, bz2, halfHeight);
            lastMinX = bx1;
            lastMinZ = bz1;
            lastMaxX = bx2;
            lastMaxZ = bz2;
            needsRebuild = false;
        }

        Vec3 camPos = cameraPos;
        float red = R / 255.0F, green = G / 255.0F, blue = B / 255.0F;
        float offset = (float)(System.currentTimeMillis() % 3000L) / 3000.0F;

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture texture = textureManager.getTexture(FORCEFIELD);

        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();
        GpuTextureView depthTexture = mainTarget.getDepthTextureView();

        var indices = getIndices();
        GpuBuffer indexBuffer = indices.getBuffer(24);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(red, green, blue, ALPHA),
                        new Vector3f((float)(lastMinX - camPos.x), (float)(-camPos.y), (float)(lastMinZ - camPos.z)),
                        new Matrix4f().translation(offset, offset, 0.0F)
                );

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Safe zone border", colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
            renderPass.setPipeline(RenderPipelines.WORLD_BORDER);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setIndexBuffer(indexBuffer, indices.type());
            renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
            renderPass.setVertexBuffer(0, this.vertexBuffer);

            List<RenderPass.Draw<SafeZoneBorderRenderer>> draws = new ArrayList<>(4);
            for (int side = 0; side < 4; side++) {
                draws.add(new RenderPass.Draw<>(0, this.vertexBuffer, indexBuffer, indices.type(), 6 * side, 6, 0));
            }
            renderPass.drawMultipleIndexed(draws, null, null, Collections.emptyList(), this);
        }
    }

    private void rebuildBuffer(double minX, double minZ, double maxX, double maxZ, float halfHeight) {
        float width = (float)(maxX - minX);
        float depth = (float)(maxZ - minZ);
        int vertexSize = DefaultVertexFormat.POSITION_TEX.getVertexSize();
        int vertexCount = 16;

        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
        this.vertexBuffer = RenderSystem.getDevice()
                .createBuffer(() -> "Safe zone border vbo", vertexCount * vertexSize, vertexCount * vertexSize);

        try (ByteBufferBuilder byteBuf = ByteBufferBuilder.exactlySized(vertexCount * vertexSize)) {
            BufferBuilder builder = new BufferBuilder(byteBuf, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            // Wall North (x: minX..maxX, z: maxZ) — vertices 0-3
            builder.addVertex(0.0F, -halfHeight, depth).setUv(0.0F, 1.0F);
            builder.addVertex(width, -halfHeight, depth).setUv(width / 2.0F, 1.0F);
            builder.addVertex(width, halfHeight, depth).setUv(width / 2.0F, 0.0F);
            builder.addVertex(0.0F, halfHeight, depth).setUv(0.0F, 0.0F);

            // Wall South (x: minX..maxX, z: minZ) — vertices 4-7
            builder.addVertex(width, -halfHeight, 0.0F).setUv(0.0F, 1.0F);
            builder.addVertex(0.0F, -halfHeight, 0.0F).setUv(width / 2.0F, 1.0F);
            builder.addVertex(0.0F, halfHeight, 0.0F).setUv(width / 2.0F, 0.0F);
            builder.addVertex(width, halfHeight, 0.0F).setUv(0.0F, 0.0F);

            // Wall East (x: maxX, z: minZ..maxZ) — vertices 8-11
            builder.addVertex(width, -halfHeight, depth).setUv(0.0F, 1.0F);
            builder.addVertex(width, -halfHeight, 0.0F).setUv(depth / 2.0F, 1.0F);
            builder.addVertex(width, halfHeight, 0.0F).setUv(depth / 2.0F, 0.0F);
            builder.addVertex(width, halfHeight, depth).setUv(0.0F, 0.0F);

            // Wall West (x: minX, z: minZ..maxZ) — vertices 12-15
            builder.addVertex(0.0F, -halfHeight, 0.0F).setUv(0.0F, 1.0F);
            builder.addVertex(0.0F, -halfHeight, depth).setUv(depth / 2.0F, 1.0F);
            builder.addVertex(0.0F, halfHeight, depth).setUv(depth / 2.0F, 0.0F);
            builder.addVertex(0.0F, halfHeight, 0.0F).setUv(0.0F, 0.0F);

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
