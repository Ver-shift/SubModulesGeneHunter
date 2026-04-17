package com.pz.beyond.api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BorderRenderUtil {
    public static void render(int centerX, int centerZ , int radius, int r, int g, int b, int bottomAlpha, int topAlpha, ResourceLocation tex, Camera camera, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double minX = centerX - radius;
        double maxX = centerX + radius;
        double minZ = centerZ - radius;
        double maxZ = centerZ + radius;

        render(minX,minZ,maxX,maxZ,r,g,b,bottomAlpha,topAlpha,tex,camera,poseStack);
    }
    public static void render(double minX,double minZ,double maxX, double maxZ,int r,int g,int b,int bottomAlpha,int topAlpha, ResourceLocation tex,Camera camera,PoseStack poseStack){
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;


        double minY = mc.level.getMinBuildHeight();
        double maxY = mc.level.getMaxBuildHeight();


        Vec3 cameraPos = camera.getPosition();

        poseStack.pushPose();

        // 平移矩阵到相机位置以实现世界空间渲染
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // 配置渲染状态：启用混合、禁用背面剔除和深度写入
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        // 设置着色器和力场纹理
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0,tex);

        // 计算基于时间的 UV 偏移，实现动态纹理效果
        float time = Util.getMillis() / 2500.0F ;
        float offsetU = time / 0.5f;
        float offsetV = -time;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        // 计算 UV 坐标的长度（用于纹理映射）
        float uLengthX = (float) (maxX - minX);
        float uLengthZ = (float) (maxZ - minZ);
        float vHeight = (float) (maxY - minY);

        addWall(bufferbuilder, matrix, minX, maxX, minY, maxY, minZ, minZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthX, vHeight);
        addWall(bufferbuilder, matrix, maxX, minX, minY, maxY, maxZ, maxZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthX, vHeight);
        addWall(bufferbuilder, matrix, minX, minX, minY, maxY, maxZ, minZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthZ, vHeight);
        addWall(bufferbuilder, matrix, maxX, maxX, minY, maxY, minZ, maxZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthZ, vHeight);
        // 提交缓冲区数据并渲染
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        // 恢复渲染状态
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();

    }



    /**
     * 添加一个面的顶点数据到缓冲区。
     *
     * @param builder 缓冲区构建器，用于存储顶点数据
     * @param matrix 模型视图投影矩阵，用于顶点变换
     * @param x1 面起始 X 坐标
     * @param x2 面结束 X 坐标
     * @param y1 面底部 Y 坐标
     * @param y2 面顶部 Y 坐标
     * @param z1 面第一个 Z 坐标
     * @param z2 面第二个 Z 坐标
     * @param r 红色分量值（0-255）
     * @param g 绿色分量值（0-255）
     * @param b 蓝色分量值（0-255）
     * @param bottomAlpha 底部透明度（0-255）
     * @param topAlpha 顶部透明度（0-255）
     * @param uOffset U 方向纹理偏移量
     * @param vOffset V 方向纹理偏移量
     * @param uMax U 方向最大长度
     * @param vMax V 方向最大高度
     */
    private static void addWall(BufferBuilder builder, Matrix4f matrix, double x1, double x2, double y1, double y2, double z1, double z2, int r, int g, int b, int bottomAlpha, int topAlpha, float uOffset, float vOffset, float uMax, float vMax) {
        builder.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(r, g, b, bottomAlpha).setUv(uOffset, vMax + vOffset);
        builder.addVertex(matrix, (float) x2, (float) y1, (float) z2).setColor(r, g, b, bottomAlpha).setUv(uMax + uOffset, vMax + vOffset);
        builder.addVertex(matrix, (float) x2, (float) y2, (float) z2).setColor(r, g, b, topAlpha).setUv(uMax + uOffset, vOffset);
        builder.addVertex(matrix, (float) x1, (float) y2, (float) z1).setColor(r, g, b, topAlpha).setUv(uOffset, vOffset);
    }
}

