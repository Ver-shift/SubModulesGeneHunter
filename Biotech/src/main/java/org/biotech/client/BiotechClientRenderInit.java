package org.biotech.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import org.biotech.Biotech;
import org.biotech.api.init.BiotechItemInit;
import org.biotech.api.tooltip.TraitTooltipComponent;
import org.biotech.client.render.XeneItemDecorator;
import org.biotech.client.tooltip.TraitClientTooltipComponent;

@EventBusSubscriber(modid = Biotech.MODID, value = Dist.CLIENT)
public class BiotechClientRenderInit {

    private static final XeneItemDecorator XENE_ITEM_DECORATOR = new XeneItemDecorator();

    @SubscribeEvent
    public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(TraitTooltipComponent.class, TraitClientTooltipComponent::new);
    }

    @SubscribeEvent
    public static void registerItemDecorators(RegisterItemDecorationsEvent event) {
        event.register(BiotechItemInit.XENE_ITEM.get(), XENE_ITEM_DECORATOR);
    }
}
