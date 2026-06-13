package org.galaxy.gene_hunter.api.system.weapon;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

public record WeaponDamageType(
        String id,
        TagKey<Item> tag,
        DeferredHolder<Attribute, Attribute> attribute,
        String descriptionKey,
        Channel channel
) {
    public enum Channel {
        GRIP,
        SHAPE
    }
}
