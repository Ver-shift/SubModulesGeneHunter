package org.galaxy.beyond.api.client.render;

import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.node.NodeColor;

public final class ZoneRenderConfig {

    private ZoneRenderConfig() {
    }

    public static boolean safeZoneBorder(ZoneRenderContext context) {
        if (!CommonConfig.ZONE_RENDER_ENABLED.get()) return false;
        if (context.debugMode()) return CommonConfig.DEBUG_SHOW_SAFE_ZONE_BORDER.get();
        return CommonConfig.RENDER_SAFE_ZONE_BORDER.get();
    }

    public static boolean activeZoneBorder(ZoneRenderContext context) {
        if (!CommonConfig.ZONE_RENDER_ENABLED.get()) return false;
        if (context.debugMode()) return CommonConfig.DEBUG_SHOW_ACTIVE_ZONE_BORDER.get();
        return context.onProgress() && CommonConfig.RENDER_ACTIVE_ZONE_BORDER.get();
    }

    public static boolean nodeColor(NodeColor color, ZoneRenderContext context) {
        if (!CommonConfig.ZONE_RENDER_ENABLED.get()) return false;
        if (!context.debugMode() && !context.onProgress()) return false;

        return switch (color == null ? NodeColor.ORANGE : color) {
            case GREEN -> context.debugMode() ? CommonConfig.DEBUG_SHOW_GREEN_NODES.get() : CommonConfig.RENDER_GREEN_NODES.get();
            case ORANGE, EMPTY -> context.debugMode() ? CommonConfig.DEBUG_SHOW_ORANGE_NODES.get() : CommonConfig.RENDER_ORANGE_NODES.get();
            case RED -> context.debugMode() ? CommonConfig.DEBUG_SHOW_RED_NODES.get() : CommonConfig.RENDER_RED_NODES.get();
            case BLUE -> context.debugMode() ? CommonConfig.DEBUG_SHOW_BLUE_NODES.get() : CommonConfig.RENDER_BLUE_NODES.get();
        };
    }

    public static boolean limitNodeCount(ZoneRenderContext context) {
        return !context.debugMode() && context.onProgress();
    }
}
