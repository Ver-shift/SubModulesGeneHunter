package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Util;
import net.minecraft.client.Camera;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeBorderRenderer extends ZoneBorderRenderer {

    private static final long DEBUG_LOG_INTERVAL_MS = 3000L;

    private long nextDebugLogMs;

    @Override
    public void render(Level level, Camera camera, PoseStack poseStack) {
        if (level == null) return;
        List<NodeBorder> borders = collectNodeBorders(BeyondAPI.getNodeDatas(level), CommonConfig.DEBUG_MODE.get());
        renderBorders(borders, camera, poseStack);
    }

    @Override
    public void render(ZoneRenderContext context) {
        List<NodeBorder> borders = collectNodeBorders(context.nodeDatas(), context);
        List<NodeBorder> allBorders = borders;
        if (ZoneRenderConfig.limitNodeCount(context) && context.limitByPlayerState()) {
            borders = nearestBorders(borders, context);
        }
        debugNearbyNodes(context, allBorders, borders);
        submitBorders(borders, context);
    }

    private static void renderBorders(List<NodeBorder> borders, Camera camera, PoseStack poseStack) {
        for (NodeBorder border : borders) {
            ZoneHelper.Bounds bounds = border.bounds();
            RenderHelper.renderBorder(
                    bounds.minX() * 16.0,
                    bounds.minZ() * 16.0,
                    (bounds.maxX() + 1) * 16.0,
                    (bounds.maxZ() + 1) * 16.0,
                    border.argb(),
                    camera, poseStack
            );
        }
    }

    private static void submitBorders(List<NodeBorder> borders, ZoneRenderContext context) {
        for (NodeBorder border : borders) {
            ZoneHelper.Bounds bounds = border.bounds();
            RenderHelper.submitBorder(
                    bounds.minX() * 16.0,
                    bounds.minZ() * 16.0,
                    (bounds.maxX() + 1) * 16.0,
                    (bounds.maxZ() + 1) * 16.0,
                    border.argb(),
                    context.camera(), context.poseStack(), context.submitNodeCollector()
            );
        }
    }

    private static List<NodeBorder> collectNodeBorders(List<NodeData> nodeDatas, boolean debugMode) {
        List<NodeBorder> borders = new ArrayList<>();
        for (NodeData nodeData : nodeDatas) {
            Set<ChunkPos> chunks = new HashSet<>(nodeData.getNodeChunkPosList());
            if (!chunks.isEmpty()) borders.add(NodeBorder.from(chunks, nodeData.getColor(), nodeData.ensureNodeKey()));
        }
        return borders;
    }

    private static List<NodeBorder> collectNodeBorders(List<NodeData> nodeDatas, ZoneRenderContext context) {
        List<NodeBorder> borders = new ArrayList<>();
        for (NodeData nodeData : nodeDatas) {
            if (!ZoneRenderConfig.nodeColor(nodeData.getColor(), context)) continue;
            Set<ChunkPos> chunks = new HashSet<>(nodeData.getNodeChunkPosList());
            if (!chunks.isEmpty()) borders.add(NodeBorder.from(chunks, nodeData.getColor(), nodeData.ensureNodeKey()));
        }
        return borders;
    }

    private static List<NodeBorder> nearestBorders(List<NodeBorder> borders, ZoneRenderContext context) {
        Map<NodeColor, List<NodeBorder>> byColor = new EnumMap<>(NodeColor.class);
        for (NodeBorder border : borders) {
            NodeColor color = border.nodeColor();
            if (color != NodeColor.GREEN && color != NodeColor.ORANGE && color != NodeColor.RED && color != NodeColor.BLUE)
                continue;

            byColor.computeIfAbsent(color, ignored -> new ArrayList<>()).add(border);
        }

        List<NodeBorder> result = new ArrayList<>();
        addNearest(result, byColor, NodeColor.GREEN, context.visibleNodeCount(NodeColor.GREEN), context.playerX(), context.playerZ());
        addNearest(result, byColor, NodeColor.ORANGE, context.visibleNodeCount(NodeColor.ORANGE), context.playerX(), context.playerZ());
        addNearest(result, byColor, NodeColor.RED, context.visibleNodeCount(NodeColor.RED), context.playerX(), context.playerZ());
        addNearest(result, byColor, NodeColor.BLUE, context.visibleNodeCount(NodeColor.BLUE), context.playerX(), context.playerZ());

        return result.stream()
                .sorted(Comparator.comparingDouble(border -> border.distanceSqr(context.playerX(), context.playerZ())))
                .toList();
    }

    private static void addNearest(List<NodeBorder> result,
                                   Map<NodeColor, List<NodeBorder>> byColor,
                                   NodeColor color,
                                   int count,
                                   double x,
                                   double z) {
        if (count <= 0) return;
        List<NodeBorder> borders = byColor.get(color);
        if (borders == null || borders.isEmpty()) return;

        borders.stream()
                .sorted(Comparator.comparingDouble(border -> border.distanceSqr(x, z)))
                .limit(count)
                .forEach(result::add);
    }

    private void debugNearbyNodes(ZoneRenderContext context, List<NodeBorder> allBorders, List<NodeBorder> renderBorders) {
        if (!CommonConfig.DEBUG_MODE.get()) return;
        long now = Util.getMillis();
        if (now < nextDebugLogMs) return;
        nextDebugLogMs = now + DEBUG_LOG_INTERVAL_MS;

        Set<Long> renderedKeys = new HashSet<>();
        for (NodeBorder border : renderBorders) {
            renderedKeys.add(border.nodeKey());
        }

        String activeNodes = nearbyNodeText(context, allBorders, renderedKeys, true);
        String outsideNodes = nearbyNodeText(context, allBorders, renderedKeys, false);

        Beyond.debugInfo(
                "[ZoneRender][NODE_NEARBY] all={}, render={}, visible G/O/R/B={}/{}/{}/{}, active={}, outside={}",
                allBorders.size(),
                renderBorders.size(),
                context.visibleNodeCount(NodeColor.GREEN),
                context.visibleNodeCount(NodeColor.ORANGE),
                context.visibleNodeCount(NodeColor.RED),
                context.visibleNodeCount(NodeColor.BLUE),
                activeNodes,
                outsideNodes
        );
    }

    private static String nearbyNodeText(ZoneRenderContext context, List<NodeBorder> borders,
                                         Set<Long> renderedKeys, boolean active) {
        StringBuilder result = new StringBuilder();
        borders.stream()
                .filter(border -> border.touchesActive(context.data()) == active)
                .sorted(Comparator.comparingDouble(border -> border.distanceSqr(context.playerX(), context.playerZ())))
                .limit(6)
                .forEach(border -> {
                    if (!result.isEmpty()) result.append(" | ");
                    result.append(border.debugText(context.playerX(), context.playerZ(), renderedKeys.contains(border.nodeKey())));
                });
        return result.toString();
    }

    private record NodeBorder(ZoneHelper.Bounds bounds, NodeColor nodeColor, int argb, double x, double z,
                              long nodeKey, Set<ChunkPos> chunks) {
        static NodeBorder from(Set<ChunkPos> chunks, NodeColor nodeColor, long nodeKey) {
            ZoneHelper.Bounds bounds = ZoneHelper.boundsOf(chunks);
            double x = (bounds.minX() + bounds.maxX() + 1) * 8.0;
            double z = (bounds.minZ() + bounds.maxZ() + 1) * 8.0;
            return new NodeBorder(bounds, nodeColor, argbOf(nodeColor), x, z, nodeKey, Set.copyOf(chunks));
        }

        private static int argbOf(NodeColor nodeColor) {
            return switch (nodeColor == null ? NodeColor.ORANGE : nodeColor) {
                case GREEN -> CommonConfig.GREEN_NODE_RENDER_COLOR.get();
                case ORANGE, EMPTY -> CommonConfig.ORANGE_NODE_RENDER_COLOR.get();
                case RED -> CommonConfig.RED_NODE_RENDER_COLOR.get();
                case BLUE -> CommonConfig.BLUE_NODE_RENDER_COLOR.get();
            };
        }

        double distanceSqr(double px, double pz) {
            double dx = px - x;
            double dz = pz - z;
            return dx * dx + dz * dz;
        }

        boolean touchesActive(LevelZoneData data) {
            for (ChunkPos chunk : chunks) {
                if (data.isActive(chunk)) return true;
                if (data.isActive(new ChunkPos(chunk.x() - 1, chunk.z()))) return true;
                if (data.isActive(new ChunkPos(chunk.x() + 1, chunk.z()))) return true;
                if (data.isActive(new ChunkPos(chunk.x(), chunk.z() - 1))) return true;
                if (data.isActive(new ChunkPos(chunk.x(), chunk.z() + 1))) return true;
            }
            return false;
        }

        String debugText(double px, double pz, boolean rendered) {
            double distance = Math.sqrt(distanceSqr(px, pz));
            return (rendered ? "*" : "-") + nodeColor + "#" + nodeKey
                    + " d=" + String.format(java.util.Locale.ROOT, "%.1f", distance)
                    + " bounds=" + bounds.minX() + "," + bounds.minZ() + ".." + bounds.maxX() + "," + bounds.maxZ()
                    + " alpha=" + RenderHelper.alpha(argb);
        }
    }
}
