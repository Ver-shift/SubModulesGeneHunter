package org.galaxy.beyond.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public record ZoneRenderContext(Level level,
                                LevelZoneData data,
                                Camera camera,
                                PoseStack poseStack,
                                List<NodeData> nodeDatas,
                                 boolean debugMode,
                                 boolean limitByPlayerState,
                                 boolean playerInSafeZone,
                                 boolean onProgress,
                                 double playerX,
                                 double playerZ,
                                double boundaryVisibleDistance,
                                Map<NodeColor, Integer> visibleNodeCounts,
                                PlayerPhase playerPhase) {

    public int visibleNodeCount(NodeColor color) {
        return visibleNodeCounts.getOrDefault(color, 0);
    }

    public static Map<NodeColor, Integer> nodeCounts(int green, int orange, int red, int blue) {
        Map<NodeColor, Integer> counts = new EnumMap<>(NodeColor.class);
        counts.put(NodeColor.GREEN, green);
        counts.put(NodeColor.ORANGE, orange);
        counts.put(NodeColor.RED, red);
        counts.put(NodeColor.BLUE, blue);
        return counts;
    }
}
