package org.galaxy.beyond.api.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
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
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var cameraPos = event.getLevelRenderState().cameraRenderState.pos;

        getSafeZoneRenderer().render(level, cameraPos, event.getPoseStack());
        getNodeBorderRenderer().render(level, cameraPos, event.getPoseStack());
        getActiveZoneRenderer().render(level, cameraPos, event.getPoseStack());
    }
}
