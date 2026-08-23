package org.galaxy.gene_hunter.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterEntityInit;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = GeneHunter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GeneHunterClientModEvents {
    private GeneHunterClientModEvents() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GeneHunterEntityInit.SLASH_WAVE.get(), SlashWaveRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SlashWaveRenderer.MODEL_LAYER, SlashWaveRenderer::createBodyLayer);
    }
}
