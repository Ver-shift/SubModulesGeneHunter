package org.biotech.api.event.handle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.biotech.Biotech;
import org.biotech.item.DefinedGeneItem;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/** Applies definition attributes with IDs unique to each equipped Curios slot. */
@EventBusSubscriber(modid = Biotech.MODID)
public final class DefinedGeneAttributeHandler {
    private DefinedGeneAttributeHandler() {}

    @SubscribeEvent
    public static void addDefinitionAttributes(CurioAttributeModifierEvent event) {
        if (!(event.getItemStack().getItem() instanceof DefinedGeneItem)) {
            return;
        }

        event.getItemStack().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                        net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
                .modifiers()
                .forEach(entry -> {
                    AttributeModifier original = entry.modifier();
                    ResourceLocation uniqueId = uniqueModifierId(event.getId(), original.id());
                    event.addModifier(entry.attribute(), new AttributeModifier(
                            uniqueId, original.amount(), original.operation()));
                });
    }

    private static ResourceLocation uniqueModifierId(ResourceLocation curioSlotId, ResourceLocation definitionId) {
        return ResourceLocation.fromNamespaceAndPath(Biotech.MODID,
                "equipped/" + curioSlotId.getNamespace() + "/" + curioSlotId.getPath()
                        + "/" + definitionId.getNamespace() + "/" + definitionId.getPath());
    }
}
