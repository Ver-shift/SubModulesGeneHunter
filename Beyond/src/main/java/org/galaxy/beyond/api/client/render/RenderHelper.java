package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.List;

public final class RenderHelper {
    public static final ResourceLocation FORCEFIELD =
            ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    private RenderHelper() {
    }

    public static void renderBorder(double minX, double minZ, double maxX, double maxZ,
                                    int r, int g, int b, int bottomAlpha, int topAlpha,
                                    Camera camera, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double minY = mc.level.getMinBuildHeight();
        double maxY = mc.level.getMaxBuildHeight();

        poseStack.pushPose();
        var cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, FORCEFIELD);

        float time = Util.getMillis() / 2500.0F;
        float offsetU = time / 0.5F;
        float offsetV = -time;
        float uLengthX = (float) (maxX - minX);
        float uLengthZ = (float) (maxZ - minZ);
        float vHeight = (float) (maxY - minY);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        addWall(builder, matrix, minX, maxX, minY, maxY, minZ, minZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthX, vHeight);
        addWall(builder, matrix, maxX, minX, minY, maxY, maxZ, maxZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthX, vHeight);
        addWall(builder, matrix, minX, minX, minY, maxY, maxZ, minZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthZ, vHeight);
        addWall(builder, matrix, maxX, maxX, minY, maxY, minZ, maxZ, r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLengthZ, vHeight);

        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void renderBorder(double minX, double minZ, double maxX, double maxZ,
                                    int argb, Camera camera, PoseStack poseStack) {
        int alpha = alpha(argb);
        if (alpha <= 0) return;
        renderBorder(
                minX, minZ, maxX, maxZ,
                red(argb), green(argb), blue(argb),
                alpha, fadeTopAlpha(alpha),
                camera, poseStack
        );
    }

    public static void renderBorderSegments(List<BorderSegment> segments,
                                            int r, int g, int b, int bottomAlpha, int topAlpha,
                                            Camera camera, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || segments.isEmpty()) return;

        double minY = mc.level.getMinBuildHeight();
        double maxY = mc.level.getMaxBuildHeight();

        poseStack.pushPose();
        var cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, FORCEFIELD);

        float time = Util.getMillis() / 2500.0F;
        float offsetU = time / 0.5F;
        float offsetV = -time;
        float vHeight = (float) (maxY - minY);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        for (BorderSegment segment : segments) {
            float uLength = (float) segment.length();
            addWall(builder, matrix,
                    segment.x1(), segment.x2(), minY, maxY, segment.z1(), segment.z2(),
                    r, g, b, bottomAlpha, topAlpha, offsetU, offsetV, uLength, vHeight);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void renderBorderSegments(List<BorderSegment> segments,
                                            int argb, Camera camera, PoseStack poseStack) {
        int alpha = alpha(argb);
        if (alpha <= 0) return;
        renderBorderSegments(
                segments,
                red(argb), green(argb), blue(argb),
                alpha, fadeTopAlpha(alpha),
                camera, poseStack
        );
    }

    private static void addWall(BufferBuilder builder, Matrix4f matrix,
                                double x1, double x2, double y1, double y2, double z1, double z2,
                                int r, int g, int b, int bottomAlpha, int topAlpha,
                                float uOffset, float vOffset, float uMax, float vMax) {
        builder.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(r, g, b, bottomAlpha).setUv(uOffset, vMax + vOffset);
        builder.addVertex(matrix, (float) x2, (float) y1, (float) z2).setColor(r, g, b, bottomAlpha).setUv(uMax + uOffset, vMax + vOffset);
        builder.addVertex(matrix, (float) x2, (float) y2, (float) z2).setColor(r, g, b, topAlpha).setUv(uMax + uOffset, vOffset);
        builder.addVertex(matrix, (float) x1, (float) y2, (float) z1).setColor(r, g, b, topAlpha).setUv(uOffset, vOffset);
    }

    public record BorderSegment(double x1, double z1, double x2, double z2) {
        double length() {
            double dx = x2 - x1;
            double dz = z2 - z1;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }

    public static int alpha(int argb) {
        return argb >>> 24;
    }

    public static int red(int argb) {
        return (argb >> 16) & 255;
    }

    public static int green(int argb) {
        return (argb >> 8) & 255;
    }

    public static int blue(int argb) {
        return argb & 255;
    }

    private static int fadeTopAlpha(int bottomAlpha) {
        return Math.max(0, Math.min(255, bottomAlpha * TOP_FADE_ALPHA / BOTTOM_FADE_ALPHA));
    }

    private static final int BOTTOM_FADE_ALPHA = 140;
    private static final int TOP_FADE_ALPHA = 30;
}
