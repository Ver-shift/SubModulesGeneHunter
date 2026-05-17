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

    private static final SafeZoneBorderRenderer safeZoneRenderer = new SafeZoneBorderRenderer();
    private static final ActiveZoneBorderRenderer activeZoneRenderer = new ActiveZoneBorderRenderer();
    private static final NodeBorderRenderer nodeBorderRenderer = new NodeBorderRenderer();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var cameraPos = event.getLevelRenderState().cameraRenderState.pos;

        safeZoneRenderer.render(level, cameraPos, event.getPoseStack());
        activeZoneRenderer.render(level, cameraPos, event.getPoseStack());
        nodeBorderRenderer.render(level, cameraPos, event.getPoseStack());
    }
}
