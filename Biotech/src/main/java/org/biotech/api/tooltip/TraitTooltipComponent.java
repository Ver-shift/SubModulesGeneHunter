package org.biotech.api.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.component.TraitComp;

import java.util.ArrayList;
import java.util.List;

/**
 * Common tooltip payload for rendering trait icon + description rows on client.
 */
public record TraitTooltipComponent(List<Entry> entries) implements TooltipComponent {

    public TraitTooltipComponent {
        entries = List.copyOf(entries);
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public static TraitTooltipComponent fromTraitComp(TraitComp comp) {
        if (comp == null || comp.isEmpty()) {
            return new TraitTooltipComponent(List.of());
        }

        List<Entry> rows = new ArrayList<>();
        for (ITrait trait : comp.getTraits()) {
            if (trait == null) {
                continue;
            }

            ResourceLocation texture = trait.getTexture();
            Component displayName = trait.getDisplayName();
            List<Component> descriptions = new ArrayList<>();
            List<? extends Component> info = trait.getUniqueInfo();
            if (info != null) {
                for (Component component : info) {
                    if (component != null) {
                        descriptions.add(component.copy());
                    }
                }
            }

            rows.add(new Entry(texture, displayName, descriptions));
        }

        return new TraitTooltipComponent(rows);
    }

    public record Entry(ResourceLocation texture, Component name, List<Component> descriptions) {

        public Entry {
            descriptions = List.copyOf(descriptions);
        }
    }
}

