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
import org.galaxy.beyond.api.client.render.ZoneRenderConfig;
import org.galaxy.beyond.api.client.render.ZoneRenderContext;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneType;

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
        if (!CommonConfig.isRogueDimension(level)) return;

        ZoneRenderContext context = createRenderContext(event, level);
        if (context == null) return;

        if (ZoneRenderConfig.safeZoneBorder(context)) {
            getSafeZoneRenderer().render(context);
        }
        if (context.playerInSafeZone()) return;

        getNodeBorderRenderer().render(context);
        if (ZoneRenderConfig.activeZoneBorder(context)) {
            getActiveZoneRenderer().render(context);
        }
    }

    private static ZoneRenderContext createRenderContext(SubmitCustomGeometryEvent event, net.minecraft.world.level.Level level) {
        try {
            LevelZoneData data = BeyondAPI.getLevelZoneData(level);
            if (data == null || !data.hasZones()) return null;

            var player = Minecraft.getInstance().player;
            var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            var cameraPos = camera.position();
            double playerX = player == null ? cameraPos.x : player.getX();
            double playerZ = player == null ? cameraPos.z : player.getZ();
            PlayerPhase playerPhase = player == null ? PlayerPhase.LOBBY : BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
            boolean playerInSafeZone = player != null && data.getZoneType(player.chunkPosition()) == ZoneType.Safe_Zone;
            boolean onProgress = BeyondAPI.getRogueData(level).getPhase() == RoguePhase.ON_PROGRESS;

            return new ZoneRenderContext(
                    level,
                    data,
                    camera,
                    event.getPoseStack(),
                    event.getSubmitNodeCollector(),
                    BeyondAPI.getNodeDatas(level),
                    CommonConfig.DEBUG_MODE.get(),
                    !playerInSafeZone,
                    playerInSafeZone,
                    onProgress,
                    playerX,
                    playerZ,
                    CommonConfig.ACTIVE_ZONE_BORDER_VISIBLE_CHUNKS.get() * 16.0,
                    ZoneRenderContext.nodeCounts(
                            CommonConfig.VISIBLE_GREEN_NODE_COUNT.get(),
                            CommonConfig.VISIBLE_ORANGE_NODE_COUNT.get(),
                            CommonConfig.VISIBLE_RED_NODE_COUNT.get(),
                            CommonConfig.VISIBLE_BLUE_NODE_COUNT.get()
                    ),
                    playerPhase
            );
        } catch (Exception ignored) {
            return null;
        }
    }
}
