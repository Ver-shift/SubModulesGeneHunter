package org.biotech.api.system.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Immutable, datapack-defined gene data.
 *
 * <p>A gene is deliberately not an item implementation and contains no executable
 * hooks. {@code GeneItem} is the single Curios item used to carry a definition.</p>
 */
public record GeneDefinition(
        Rarity rarity,
        Component description,
        ItemAttributeModifiers attributes
) {
    public static final Codec<GeneDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rarity.CODEC.fieldOf("rarity").forGetter(GeneDefinition::rarity),
            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(GeneDefinition::description),
            ItemAttributeModifiers.CODEC.optionalFieldOf("attributes", ItemAttributeModifiers.EMPTY).forGetter(GeneDefinition::attributes)
    ).apply(instance, GeneDefinition::new));
}
