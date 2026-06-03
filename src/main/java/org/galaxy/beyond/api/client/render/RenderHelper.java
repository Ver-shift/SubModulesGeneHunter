package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import java.util.List;

public final class RenderHelper {
    public static final Identifier FORCEFIELD =
            Identifier.withDefaultNamespace("textures/misc/forcefield.png");
    private static final RenderType BORDER_RENDER_TYPE = RenderTypes.beaconBeam(FORCEFIELD, true);
    private static final float TEXTURE_SCALE = 0.5F;
    private static final int BOTTOM_FADE_ALPHA = 140;
    private static final int TOP_FADE_ALPHA = 30;

    private RenderHelper() {
    }

    public static void renderBorder(double minX, double minZ, double maxX, double maxZ,
                                    int r, int g, int b, int bottomAlpha, int topAlpha,
                                    Camera camera, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        BorderRenderState state = BorderRenderState.create(mc.level, mc, camera, r, g, b, bottomAlpha, topAlpha);
        List<BorderSegment> segments = rectangleSegments(minX, minZ, maxX, maxZ);

        poseStack.pushPose();
        translateToCamera(poseStack, camera);

        VertexConsumer builder = mc.renderBuffers().bufferSource().getBuffer(BORDER_RENDER_TYPE);
        renderSegments(builder, poseStack.last().pose(), segments, state);
        mc.renderBuffers().bufferSource().endBatch(BORDER_RENDER_TYPE);

        poseStack.popPose();
    }

    public static void submitBorder(double minX, double minZ, double maxX, double maxZ,
                                    int r, int g, int b, int bottomAlpha, int topAlpha,
                                    Camera camera, PoseStack poseStack, SubmitNodeCollector collector) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (collector == null) {
            renderBorder(minX, minZ, maxX, maxZ, r, g, b, bottomAlpha, topAlpha, camera, poseStack);
            return;
        }

        BorderRenderState state = BorderRenderState.create(mc.level, mc, camera, r, g, b, bottomAlpha, topAlpha);
        List<BorderSegment> segments = rectangleSegments(minX, minZ, maxX, maxZ);

        submitSegments(segments, camera, poseStack, collector, state);
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

    public static void submitBorder(double minX, double minZ, double maxX, double maxZ,
                                    int argb, Camera camera, PoseStack poseStack, SubmitNodeCollector collector) {
        int alpha = alpha(argb);
        if (alpha <= 0) return;
        submitBorder(
                minX, minZ, maxX, maxZ,
                red(argb), green(argb), blue(argb),
                alpha, fadeTopAlpha(alpha),
                camera, poseStack, collector
        );
    }

    public static void renderBorderSegments(List<BorderSegment> segments,
                                            int r, int g, int b, int bottomAlpha, int topAlpha,
                                            Camera camera, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || segments.isEmpty()) return;

        BorderRenderState state = BorderRenderState.create(mc.level, mc, camera, r, g, b, bottomAlpha, topAlpha);
        poseStack.pushPose();
        translateToCamera(poseStack, camera);

        VertexConsumer builder = mc.renderBuffers().bufferSource().getBuffer(BORDER_RENDER_TYPE);
        renderSegments(builder, poseStack.last().pose(), segments, state);
        mc.renderBuffers().bufferSource().endBatch(BORDER_RENDER_TYPE);

        poseStack.popPose();
    }

    public static void submitBorderSegments(List<BorderSegment> segments,
                                            int r, int g, int b, int bottomAlpha, int topAlpha,
                                            Camera camera, PoseStack poseStack, SubmitNodeCollector collector) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || segments.isEmpty()) return;
        if (collector == null) {
            renderBorderSegments(segments, r, g, b, bottomAlpha, topAlpha, camera, poseStack);
            return;
        }

        BorderRenderState state = BorderRenderState.create(mc.level, mc, camera, r, g, b, bottomAlpha, topAlpha);
        submitSegments(segments, camera, poseStack, collector, state);
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

    public static void submitBorderSegments(List<BorderSegment> segments,
                                            int argb, Camera camera, PoseStack poseStack,
                                            SubmitNodeCollector collector) {
        int alpha = alpha(argb);
        if (alpha <= 0) return;
        submitBorderSegments(
                segments,
                red(argb), green(argb), blue(argb),
                alpha, fadeTopAlpha(alpha),
                camera, poseStack, collector
        );
    }

    private static void submitSegments(List<BorderSegment> segments, Camera camera, PoseStack poseStack,
                                       SubmitNodeCollector collector, BorderRenderState state) {
        poseStack.pushPose();
        translateToCamera(poseStack, camera);
        collector.submitCustomGeometry(poseStack, BORDER_RENDER_TYPE, (pose, builder) ->
                renderSegments(builder, pose.pose(), segments, state));
        poseStack.popPose();
    }

    private static void renderSegments(VertexConsumer builder, Matrix4f matrix,
                                       List<BorderSegment> segments, BorderRenderState state) {
        for (BorderSegment segment : segments) {
            addWall(builder, matrix, segment, state);
        }
    }

    private static void addWall(VertexConsumer builder, Matrix4f matrix,
                                BorderSegment segment, BorderRenderState state) {
        double minY = state.minY();
        double maxY = state.maxY();
        float bottomV = state.bottomV();
        float topV = state.topV();
        float startU = state.u(segment.x1(), segment.z1());
        float endU = startU + (float) segment.length() * TEXTURE_SCALE;
        int bottomAlpha = state.alpha();
        int topAlpha = fadeTopAlpha(bottomAlpha);
        if (bottomAlpha <= 0) return;

        addQuad(builder, matrix, segment, state, minY, maxY, bottomAlpha, topAlpha, startU, endU, bottomV, topV, false);
        addQuad(builder, matrix, segment, state, minY, maxY, bottomAlpha, topAlpha, startU, endU, bottomV, topV, true);
    }

    private static void addQuad(VertexConsumer builder, Matrix4f matrix,
                                BorderSegment segment, BorderRenderState state,
                                double minY, double maxY, int bottomAlpha, int topAlpha,
                                float startU, float endU, float bottomV, float topV,
                                boolean reversed) {
        if (reversed) {
            addVertex(builder, matrix, segment.x1(), maxY, segment.z1(), state.r(), state.g(), state.b(), topAlpha, startU, topV);
            addVertex(builder, matrix, segment.x2(), maxY, segment.z2(), state.r(), state.g(), state.b(), topAlpha, endU, topV);
            addVertex(builder, matrix, segment.x2(), minY, segment.z2(), state.r(), state.g(), state.b(), bottomAlpha, endU, bottomV);
            addVertex(builder, matrix, segment.x1(), minY, segment.z1(), state.r(), state.g(), state.b(), bottomAlpha, startU, bottomV);
            return;
        }

        addVertex(builder, matrix, segment.x1(), minY, segment.z1(), state.r(), state.g(), state.b(), bottomAlpha, startU, bottomV);
        addVertex(builder, matrix, segment.x2(), minY, segment.z2(), state.r(), state.g(), state.b(), bottomAlpha, endU, bottomV);
        addVertex(builder, matrix, segment.x2(), maxY, segment.z2(), state.r(), state.g(), state.b(), topAlpha, endU, topV);
        addVertex(builder, matrix, segment.x1(), maxY, segment.z1(), state.r(), state.g(), state.b(), topAlpha, startU, topV);
    }

    private static void addVertex(VertexConsumer builder, Matrix4f matrix,
                                  double x, double y, double z,
                                  int r, int g, int b, int alpha, float u, float v) {
        builder.addVertex(matrix, (float) x, (float) y, (float) z)
                .setColor(r, g, b, alpha)
                .setUv(u, v)
                .setUv2(LightCoordsUtil.FULL_BRIGHT & 65535, LightCoordsUtil.FULL_BRIGHT >> 16 & 65535);
    }

    private static void translateToCamera(PoseStack poseStack, Camera camera) {
        var cameraPos = camera.position();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
    }

    private static List<BorderSegment> rectangleSegments(double minX, double minZ, double maxX, double maxZ) {
        return List.of(
                new BorderSegment(minX, minZ, maxX, minZ),
                new BorderSegment(maxX, maxZ, minX, maxZ),
                new BorderSegment(minX, maxZ, minX, minZ),
                new BorderSegment(maxX, minZ, maxX, maxZ)
        );
    }

    public record BorderSegment(double x1, double z1, double x2, double z2) {
        double length() {
            return Math.sqrt(lengthSqr());
        }

        double lengthSqr() {
            double dx = x2 - x1;
            double dz = z2 - z1;
            return dx * dx + dz * dz;
        }

    }

    private record BorderRenderState(double minY, double maxY,
                                     int r, int g, int b, int alpha,
                                     float topV, float bottomV) {
        static BorderRenderState create(Level level, Minecraft mc, Camera camera,
                                        int r, int g, int b, int bottomAlpha, int topAlpha) {
            var cameraPos = camera.position();
            double height = Math.max(level.getMaxY() - level.getMinY(), mc.options.getEffectiveRenderDistance() * 32.0);
            float v0 = (float) (-Mth.frac(cameraPos.y * TEXTURE_SCALE));
            float v1 = v0 + (float) (height * TEXTURE_SCALE);
            int alpha = Math.max(bottomAlpha, topAlpha);
            return new BorderRenderState(
                    cameraPos.y - height * 0.5,
                    cameraPos.y + height * 0.5,
                    r, g, b, Mth.clamp(alpha, 0, 255),
                    v0, v1
            );
        }

        float u(double x, double z) {
            return ((Mth.floor(Math.abs(x) > Math.abs(z) ? x : z) & 1) * 0.5F)
                    + (float) (Util.getMillis() % 3000L) / 3000.0F;
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
        return Mth.clamp(bottomAlpha * TOP_FADE_ALPHA / BOTTOM_FADE_ALPHA, 0, 255);
    }
}
