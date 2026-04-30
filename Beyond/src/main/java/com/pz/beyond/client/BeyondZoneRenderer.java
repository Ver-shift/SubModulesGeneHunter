package com.pz.beyond.client;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.init.BeyondAttachInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * 客户端区域渲染事件订阅器。
 * <p>
 * 当前仅渲染 SafeZone 的外部边界，后续需要绘制 PlayerActiveZone / NodeZone 边界时，
 * 可在此追加逻辑或为对应的 Manager 增加 renderBorder 入口。
 */
@EventBusSubscriber(modid = Beyond.MODID, value = Dist.CLIENT)
public class BeyondZoneRenderer {

    /**
     * 在半透明方块渲染之后绘制屏障，避免 alpha 混合覆盖方块像素。
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        if (!BeyondAttachInit.isAllowedDimension(level)) {
            return;
        }

        BeyondAPI.getBeyondManager()
                .getSafeZoneManager()
                .renderBorder(level, event.getPoseStack(), mc.gameRenderer.getMainCamera());
    }
}
