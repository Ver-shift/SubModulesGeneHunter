package org.galaxy.gene_hunter.api.system.weapon;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit.PlayerAttribute;

public record WeaponDamageType(
        String id,
        TagKey<Item> tag,
        PlayerAttribute attribute,
        String descriptionKey,
        Channel channel
) {
    public enum Channel {
        GRIP,
        SHAPE
    }
}
