package org.galaxy.beyond.api.system.zone;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.node.NodeColor;

public class NodeColorPicker {

    public NodeColor random(ServerLevel level) {
        int green = CommonConfig.NODE_COLOR_GREEN_WEIGHT.get();
        int orange = CommonConfig.NODE_COLOR_ORANGE_WEIGHT.get();
        int red = CommonConfig.NODE_COLOR_RED_WEIGHT.get();
        int total = green + orange + red;
        if (total <= 0) return NodeColor.ORANGE;
        int roll = level.getRandom().nextInt(total);
        if (roll < green) return NodeColor.GREEN;
        if (roll < green + orange) return NodeColor.ORANGE;
        return NodeColor.RED;
    }
}
