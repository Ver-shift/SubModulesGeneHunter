package org.galaxy.beyond.api.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.client.gui.scene.SceneUILayer;
import org.galaxy.beyond.api.client.render.ActiveZoneBorderRenderer;
import org.galaxy.beyond.api.client.render.NodeBorderRenderer;
import org.galaxy.beyond.api.client.render.SafeZoneBorderRenderer;

@EventBusSubscriber(value = Dist.CLIENT)
public class BeyondClientEventHandler {

    private static SafeZoneBorderRenderer safeZoneRenderer;
    private static NodeBorderRenderer nodeBorderRenderer;
    private static ActiveZoneBorderRenderer activeZoneRenderer;

    private static SafeZoneBorderRenderer getSafeZoneRenderer() {
        if (safeZoneRenderer == null) safeZoneRenderer = new SafeZoneBorderRenderer();
        return safeZoneRenderer;
    }

    private static NodeBorderRenderer getNodeBorderRenderer() {
        if (nodeBorderRenderer == null) nodeBorderRenderer = new NodeBorderRenderer();
        return nodeBorderRenderer;
    }

    private static ActiveZoneBorderRenderer getActiveZoneRenderer() {
        if (activeZoneRenderer == null) activeZoneRenderer = new ActiveZoneBorderRenderer();
        return activeZoneRenderer;
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {


        event.registerAboveAll(Beyond.asResource("scene_ui"), new SceneUILayer());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        getSafeZoneRenderer().render(level, event.getCamera(), event.getPoseStack());
        getNodeBorderRenderer().render(level, event.getCamera(), event.getPoseStack());
        getActiveZoneRenderer().render(level, event.getCamera(), event.getPoseStack());
    }
}
