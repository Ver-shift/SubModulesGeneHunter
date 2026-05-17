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

public final class RenderHelper {

    public static final Identifier FORCEFIELD =
            Identifier.withDefaultNamespace("textures/misc/forcefield.png");

    private RenderHelper() {}

    /**
     * 向 BufferBuilder 写入模型空间的 4 面墙（North / South / East / West），
     * 顶点格式为 POSITION_TEX，用于 WORLD_BORDER 管道。
     */
    public static void writeBoxWalls(BufferBuilder builder, float width, float depth, float halfHeight) {
        // North (z = depth)
        builder.addVertex(0.0F, -halfHeight, depth).setUv(0.0F, 1.0F);
        builder.addVertex(width, -halfHeight, depth).setUv(width / 2.0F, 1.0F);
        builder.addVertex(width, halfHeight, depth).setUv(width / 2.0F, 0.0F);
        builder.addVertex(0.0F, halfHeight, depth).setUv(0.0F, 0.0F);

        // South (z = 0)
        builder.addVertex(width, -halfHeight, 0.0F).setUv(0.0F, 1.0F);
        builder.addVertex(0.0F, -halfHeight, 0.0F).setUv(width / 2.0F, 1.0F);
        builder.addVertex(0.0F, halfHeight, 0.0F).setUv(width / 2.0F, 0.0F);
        builder.addVertex(width, halfHeight, 0.0F).setUv(0.0F, 0.0F);

        // East (x = width)
        builder.addVertex(width, -halfHeight, depth).setUv(0.0F, 1.0F);
        builder.addVertex(width, -halfHeight, 0.0F).setUv(depth / 2.0F, 1.0F);
        builder.addVertex(width, halfHeight, 0.0F).setUv(depth / 2.0F, 0.0F);
        builder.addVertex(width, halfHeight, depth).setUv(0.0F, 0.0F);

        // West (x = 0)
        builder.addVertex(0.0F, -halfHeight, 0.0F).setUv(0.0F, 1.0F);
        builder.addVertex(0.0F, -halfHeight, depth).setUv(depth / 2.0F, 1.0F);
        builder.addVertex(0.0F, halfHeight, depth).setUv(depth / 2.0F, 0.0F);
        builder.addVertex(0.0F, halfHeight, 0.0F).setUv(0.0F, 0.0F);
    }

    public static RenderContext captureRenderContext() {
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        AbstractTexture tex = tm.getTexture(FORCEFIELD);
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        return new RenderContext(tex, mainTarget.getColorTextureView(), mainTarget.getDepthTextureView());
    }

    public static void bindPassState(RenderPass pass, RenderContext ctx,
                                      GpuBufferSlice dynamicTransforms,
                                      GpuBuffer indexBuffer, RenderSystem.AutoStorageIndexBuffer autoIndices,
                                      GpuBuffer vertexBuffer) {
        pass.setPipeline(RenderPipelines.WORLD_BORDER);
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", dynamicTransforms);
        pass.setIndexBuffer(indexBuffer, autoIndices.type());
        pass.bindTexture("Sampler0", ctx.texture().getTextureView(), ctx.texture().getSampler());
        pass.setVertexBuffer(0, vertexBuffer);
    }

    public record RenderContext(AbstractTexture texture, GpuTextureView colorTarget, GpuTextureView depthTarget) {}
}
