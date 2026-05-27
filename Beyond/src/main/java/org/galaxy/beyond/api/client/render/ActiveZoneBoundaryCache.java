package org.galaxy.beyond.api.client.render;

import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.zone.ZoneHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class ActiveZoneBoundaryCache {
    private static final int SEGMENTS = 64;
    private static final double PADDING_BLOCKS = 1.0;
    private static final double TAU = Math.PI * 2.0;

    private Set<ChunkPos> cachedChunks = Set.of();
    private List<RenderHelper.BorderSegment> cachedSegments = List.of();

    List<RenderHelper.BorderSegment> getSegments(Set<ChunkPos> activeChunks) {
        if (!cachedChunks.equals(activeChunks)) rebuild(activeChunks);
        return cachedSegments;
    }

    void invalidate() {
        cachedChunks = Set.of();
        cachedSegments = List.of();
    }

    private void rebuild(Set<ChunkPos> activeChunks) {
        cachedChunks = Set.copyOf(activeChunks);
        if (activeChunks.isEmpty()) {
            cachedSegments = List.of();
            return;
        }

        List<Circle> circles = new ArrayList<>();
        for (Set<ChunkPos> component : ZoneHelper.findConnectedComponents(activeChunks)) {
            circles.add(Circle.from(ZoneHelper.boundsOf(component)));
        }

        List<RenderHelper.BorderSegment> segments = new ArrayList<>();
        for (int i = 0; i < circles.size(); i++) {
            addVisibleSegments(segments, circles, i, circles.get(i));
        }
        cachedSegments = List.copyOf(segments);
    }

    private static void addVisibleSegments(List<RenderHelper.BorderSegment> segments,
                                           List<Circle> circles,
                                           int circleIndex,
                                           Circle circle) {
        for (int i = 0; i < SEGMENTS; i++) {
            double a1 = TAU * i / SEGMENTS;
            double a2 = TAU * (i + 1) / SEGMENTS;
            double mid = (a1 + a2) * 0.5;
            double midX = circle.x() + Math.cos(mid) * circle.radius();
            double midZ = circle.z() + Math.sin(mid) * circle.radius();
            if (isInsideAnotherCircle(circles, circleIndex, midX, midZ)) continue;

            double x1 = circle.x() + Math.cos(a1) * circle.radius();
            double z1 = circle.z() + Math.sin(a1) * circle.radius();
            double x2 = circle.x() + Math.cos(a2) * circle.radius();
            double z2 = circle.z() + Math.sin(a2) * circle.radius();
            segments.add(new RenderHelper.BorderSegment(x1, z1, x2, z2));
        }
    }

    private static boolean isInsideAnotherCircle(List<Circle> circles, int currentIndex, double x, double z) {
        for (int i = 0; i < circles.size(); i++) {
            if (i == currentIndex) continue;
            if (circles.get(i).contains(x, z)) return true;
        }
        return false;
    }

    private record Circle(double x, double z, double radius) {
        static Circle from(ZoneHelper.Bounds bounds) {
            int widthChunks = bounds.maxX() - bounds.minX() + 1;
            int depthChunks = bounds.maxZ() - bounds.minZ() + 1;
            double x = (bounds.minX() + bounds.maxX() + 1) * 8.0;
            double z = (bounds.minZ() + bounds.maxZ() + 1) * 8.0;
            double radius = Math.max(widthChunks, depthChunks) * 8.0 + PADDING_BLOCKS;
            return new Circle(x, z, radius);
        }

        boolean contains(double px, double pz) {
            double dx = px - x;
            double dz = pz - z;
            return dx * dx + dz * dz < radius * radius;
        }
    }
}
