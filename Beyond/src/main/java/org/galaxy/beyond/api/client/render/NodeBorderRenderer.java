package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.zone.ZoneHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeBorderRenderer extends ZoneBorderRenderer {
    @Override
    public void render(Level level, Camera camera, PoseStack poseStack) {
        if (level == null) return;
        List<NodeBorder> borders = collectNodeBorders(BeyondAPI.getNodeDatas(level));
        renderBorders(borders, camera, poseStack);
    }

    @Override
    public void render(ZoneRenderContext context) {
        List<NodeBorder> borders = collectNodeBorders(context.nodeDatas());
        if (!context.debugMode() && context.limitByPlayerState()) {
            borders = nearestBorders(borders, context);
        }
        renderBorders(borders, context.camera(), context.poseStack());
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

    private static List<NodeBorder> collectNodeBorders(List<NodeData> nodeDatas) {
        List<NodeBorder> borders = new ArrayList<>();
        for (NodeData nodeData : nodeDatas) {
            Set<ChunkPos> chunks = new HashSet<>(nodeData.getNodeChunkPosList());
            if (!chunks.isEmpty()) borders.add(NodeBorder.from(chunks, nodeData.getColor()));
        }
        return borders;
    }

    private static List<NodeBorder>  nearestBorders(List<NodeBorder> borders, ZoneRenderContext context) {
        Map<NodeColor, List<NodeBorder>> byColor = new EnumMap<>(NodeColor.class);
        for (NodeBorder border : borders) {
            NodeColor color = border.nodeColor();
            if (color != NodeColor.GREEN && color != NodeColor.ORANGE && color != NodeColor.RED && color != NodeColor.BLUE) continue;

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

    private record NodeBorder(ZoneHelper.Bounds bounds, NodeColor nodeColor, int argb, double x, double z) {
        static NodeBorder from(Set<ChunkPos> chunks, NodeColor nodeColor) {
            ZoneHelper.Bounds bounds = ZoneHelper.boundsOf(chunks);
            double x = (bounds.minX() + bounds.maxX() + 1) * 8.0;
            double z = (bounds.minZ() + bounds.maxZ() + 1) * 8.0;
            return new NodeBorder(bounds, nodeColor, argbOf(nodeColor), x, z);
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
    }
}
