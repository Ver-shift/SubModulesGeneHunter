package org.galaxy.beyond.api.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.galaxy.beyond.api.client.render.SafeZoneBorderRenderer;

@EventBusSubscriber(value = Dist.CLIENT)
public class BeyondClientEventHandler {

    private static final SafeZoneBorderRenderer borderRenderer = new SafeZoneBorderRenderer();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var cameraPos = event.getLevelRenderState().cameraRenderState.pos;

        borderRenderer.render(level, cameraPos, event.getPoseStack());
    }
}
