package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ActiveZoneBorderRenderer extends ZoneBorderRenderer {
    @Override
    public void render(Level level, Camera camera, PoseStack poseStack) {
        if (level == null) return;
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        if (data == null || !data.hasZones()) return;

        var cameraPos = camera.getPosition();
        List<RenderHelper.BorderSegment> segments = nearbyBoundarySegments(data, cameraPos.x, cameraPos.z,
                CommonConfig.ACTIVE_ZONE_BORDER_VISIBLE_CHUNKS.get() * 16.0);
        RenderHelper.renderBorderSegments(segments, CommonConfig.ACTIVE_ZONE_RENDER_COLOR.get(), camera, poseStack);
    }

    @Override
    public void render(ZoneRenderContext context) {
        if (!ZoneRenderConfig.activeZoneBorder(context)) return;
        LevelZoneData data = context.data();
        if (data == null || !data.hasZones()) return;

        List<RenderHelper.BorderSegment> segments = context.debugMode()
                ? allBoundarySegments(data)
                : nearbyBoundarySegments(data, context.playerX(), context.playerZ(), context.boundaryVisibleDistance());
        RenderHelper.renderBorderSegments(segments, CommonConfig.ACTIVE_ZONE_RENDER_COLOR.get(), context.camera(), context.poseStack());
    }

    private static List<RenderHelper.BorderSegment> allBoundarySegments(LevelZoneData data) {
        Set<ChunkPos> chunks = data.activeChunks();
        if (chunks.isEmpty()) return List.of();

        List<RenderHelper.BorderSegment> result = new ArrayList<>();
        for (ChunkPos pos : chunks) {
            addBoundarySegment(result, data, pos, Direction.WEST);
            addBoundarySegment(result, data, pos, Direction.EAST);
            addBoundarySegment(result, data, pos, Direction.NORTH);
            addBoundarySegment(result, data, pos, Direction.SOUTH);
        }
        return result;
    }

    private static List<RenderHelper.BorderSegment> nearbyBoundarySegments(LevelZoneData data, double x, double z, double distance) {
        if (distance <= 0.0) return List.of();

        List<RenderHelper.BorderSegment> result = new ArrayList<>();
        int centerX = blockToChunk(x);
        int centerZ = blockToChunk(z);
        int radius = Math.max(1, (int) Math.ceil(distance / 16.0) + 1);
        double maxDistanceSqr = distance * distance;

        for (int cx = centerX - radius; cx <= centerX + radius; cx++) {
            for (int cz = centerZ - radius; cz <= centerZ + radius; cz++) {
                ChunkPos pos = new ChunkPos(cx, cz);
                if (!data.isActive(pos)) continue;

                addBoundarySegment(result, data, pos, x, z, maxDistanceSqr, Direction.WEST);
                addBoundarySegment(result, data, pos, x, z, maxDistanceSqr, Direction.EAST);
                addBoundarySegment(result, data, pos, x, z, maxDistanceSqr, Direction.NORTH);
                addBoundarySegment(result, data, pos, x, z, maxDistanceSqr, Direction.SOUTH);
            }
        }
        return result;
    }

    private static void addBoundarySegment(List<RenderHelper.BorderSegment> result, LevelZoneData data, ChunkPos pos,
                                           double x, double z, double maxDistanceSqr, Direction direction) {
        ChunkPos neighbor = switch (direction) {
            case WEST -> new ChunkPos(pos.x - 1, pos.z);
            case EAST -> new ChunkPos(pos.x + 1, pos.z);
            case NORTH -> new ChunkPos(pos.x, pos.z - 1);
            case SOUTH -> new ChunkPos(pos.x, pos.z + 1);
        };
        if (data.hasAny(neighbor)) return;

        double minX = pos.getMinBlockX();
        double minZ = pos.getMinBlockZ();
        double maxX = minX + 16.0;
        double maxZ = minZ + 16.0;
        RenderHelper.BorderSegment segment = switch (direction) {
            case WEST -> new RenderHelper.BorderSegment(minX, minZ, minX, maxZ);
            case EAST -> new RenderHelper.BorderSegment(maxX, minZ, maxX, maxZ);
            case NORTH -> new RenderHelper.BorderSegment(minX, minZ, maxX, minZ);
            case SOUTH -> new RenderHelper.BorderSegment(minX, maxZ, maxX, maxZ);
        };
        if (distanceToSegmentSqr(x, z, segment) <= maxDistanceSqr) result.add(segment);
    }

    private static void addBoundarySegment(List<RenderHelper.BorderSegment> result, LevelZoneData data, ChunkPos pos,
                                           Direction direction) {
        ChunkPos neighbor = switch (direction) {
            case WEST -> new ChunkPos(pos.x - 1, pos.z);
            case EAST -> new ChunkPos(pos.x + 1, pos.z);
            case NORTH -> new ChunkPos(pos.x, pos.z - 1);
            case SOUTH -> new ChunkPos(pos.x, pos.z + 1);
        };
        if (data.hasAny(neighbor)) return;

        double minX = pos.getMinBlockX();
        double minZ = pos.getMinBlockZ();
        double maxX = minX + 16.0;
        double maxZ = minZ + 16.0;
        result.add(switch (direction) {
            case WEST -> new RenderHelper.BorderSegment(minX, minZ, minX, maxZ);
            case EAST -> new RenderHelper.BorderSegment(maxX, minZ, maxX, maxZ);
            case NORTH -> new RenderHelper.BorderSegment(minX, minZ, maxX, minZ);
            case SOUTH -> new RenderHelper.BorderSegment(minX, maxZ, maxX, maxZ);
        });
    }

    private static int blockToChunk(double value) {
        return (int) Math.floor(value / 16.0);
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

    private enum Direction {
        WEST,
        EAST,
        NORTH,
        SOUTH
    }
}
