package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Zone 边框渲染基类 —— 统一管线：采集 context → 写 transform → bind → draw。
 * 子类只需实现 rebuildBuffer 决定渲染形状。
 */
public abstract class ZoneBorderRenderer {

    protected GpuBuffer vertexBuffer;
    protected int indexCount;
    protected final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
    protected boolean needsRebuild = true;

    /** 子类在 rebuild 里填充 vertexBuffer 并设置 indexCount */
    protected abstract void rebuildBuffer();

    protected void ensureBuffer(int vertexCount) {
        int vertexSize = com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX.getVertexSize();
        if (this.vertexBuffer != null) this.vertexBuffer.close();
        this.vertexBuffer = RenderSystem.getDevice()
                .createBuffer(() -> getClass().getSimpleName() + " vbo",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                        (long) vertexCount * vertexSize);
    }

    /** 统一渲染管线：子类只需提供颜色和 cameraPos */
    protected void drawBorder(String label, float r, float g, float b, float alpha,
                              double offsetX, double offsetY, double offsetZ, int firstVertex, int indexCount) {
        if (indexCount == 0) return;
        var ctx = RenderHelper.captureRenderContext();
        GpuBuffer indexBuffer = this.indices.getBuffer(indexCount);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(r, g, b, alpha),
                        new Vector3f((float) offsetX, (float) offsetY, (float) offsetZ),
                        new Matrix4f().translation(
                                (float) (System.currentTimeMillis() % 3000L) / 3000.0F,
                                (float) (System.currentTimeMillis() % 3000L) / 3000.0F, 0.0F));

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> label, ctx.colorTarget(), OptionalInt.empty(), ctx.depthTarget(), OptionalDouble.empty())) {
            RenderHelper.bindPassState(renderPass, ctx, dynamicTransforms, indexBuffer, this.indices, this.vertexBuffer);
            renderPass.drawMultipleIndexed(
                    List.of(new RenderPass.Draw<>(firstVertex, this.vertexBuffer, indexBuffer, this.indices.type(), 0, indexCount, 0)),
                    null, null, Collections.emptyList(), this);
        }
    }

    /** 默认 firstVertex=0 */
    protected void drawBorder(String label, float r, float g, float b, float alpha,
                              double offsetX, double offsetY, double offsetZ) {
        drawBorder(label, r, g, b, alpha, offsetX, offsetY, offsetZ, 0, indexCount);
    }

    public void invalidate() {
        this.needsRebuild = true;
    }
}
