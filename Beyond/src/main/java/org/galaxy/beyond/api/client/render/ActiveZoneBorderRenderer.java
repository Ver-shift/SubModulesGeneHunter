package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ActiveZoneBorderRenderer extends ZoneBorderRenderer {
    private final ActiveZoneBoundaryCache boundaryCache = new ActiveZoneBoundaryCache();

    @Override
    public void render(Level level, Camera camera, PoseStack poseStack) {
        if (level == null) return;
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        if (data == null || !data.hasZones()) return;

        Set<ChunkPos> activeChunks = data.activeChunks();
        if (activeChunks.isEmpty()) return;

        List<RenderHelper.BorderSegment> segments = boundaryCache.getSegments(activeChunks);
        RenderHelper.renderBorderSegments(segments, 220, 220, 220, 100, 25, camera, poseStack);
    }

    @Override
    public void render(ZoneRenderContext context) {
        LevelZoneData data = context.data();
        if (data == null || !data.hasZones()) return;

        Set<ChunkPos> activeChunks = data.activeChunks();
        if (activeChunks.isEmpty()) return;

        List<RenderHelper.BorderSegment> segments = boundaryCache.getSegments(activeChunks);
        if (!context.debugMode()) {
            segments = nearbySegments(segments, context);
        }

        RenderHelper.renderBorderSegments(segments, 220, 220, 220, 100, 25, context.camera(), context.poseStack());
    }

    @Override
    public void invalidate() {
        boundaryCache.invalidate();
    }

    private static List<RenderHelper.BorderSegment> nearbySegments(List<RenderHelper.BorderSegment> segments, ZoneRenderContext context) {
        double maxDistanceSqr = context.boundaryVisibleDistance() * context.boundaryVisibleDistance();

        List<RenderHelper.BorderSegment> result = new ArrayList<>();
        for (RenderHelper.BorderSegment segment : segments) {
            if (distanceToSegmentSqr(context.playerX(), context.playerZ(), segment) <= maxDistanceSqr) result.add(segment);
        }
        return result;
    }

    private static double distanceToSegmentSqr(double x, double z, RenderHelper.BorderSegment segment) {
        double dx = segment.x2() - segment.x1();
        double dz = segment.z2() - segment.z1();
        double lengthSqr = dx * dx + dz * dz;
        if (lengthSqr <= 0.0) return distanceSqr(x, z, segment.x1(), segment.z1());

        double t = ((x - segment.x1()) * dx + (z - segment.z1()) * dz) / lengthSqr;
        t = Math.max(0.0, Math.min(1.0, t));
        return distanceSqr(x, z, segment.x1() + dx * t, segment.z1() + dz * t);
    }

    private static double distanceSqr(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return dx * dx + dz * dz;
    }
}
