package org.galaxy.beyond.api.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.galaxy.beyond.api.client.render.ActiveZoneBorderRenderer;
import org.galaxy.beyond.api.client.render.NodeBorderRenderer;
import org.galaxy.beyond.api.client.render.SafeZoneBorderRenderer;

@EventBusSubscriber(value = Dist.CLIENT)
public class BeyondClientEventHandler {

    private static SafeZoneBorderRenderer safeZoneRenderer;
    private static ActiveZoneBorderRenderer activeZoneRenderer;
    private static NodeBorderRenderer nodeBorderRenderer;

    private static SafeZoneBorderRenderer getSafeZoneRenderer() {
        if (safeZoneRenderer == null) safeZoneRenderer = new SafeZoneBorderRenderer();
        return safeZoneRenderer;
    }

    private static ActiveZoneBorderRenderer getActiveZoneRenderer() {
        if (activeZoneRenderer == null) activeZoneRenderer = new ActiveZoneBorderRenderer();
        return activeZoneRenderer;
    }

    private static NodeBorderRenderer getNodeBorderRenderer() {
        if (nodeBorderRenderer == null) nodeBorderRenderer = new NodeBorderRenderer();
        return nodeBorderRenderer;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var cameraPos = event.getLevelRenderState().cameraRenderState.pos;

        getSafeZoneRenderer().render(level, cameraPos, event.getPoseStack());
        getActiveZoneRenderer().render(level, cameraPos, event.getPoseStack());
        getNodeBorderRenderer().render(level, cameraPos, event.getPoseStack());
    }
}
