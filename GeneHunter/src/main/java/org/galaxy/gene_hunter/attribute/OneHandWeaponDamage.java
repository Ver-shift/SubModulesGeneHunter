package org.galaxy.gene_hunter.attribute;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;

/**
 * 武器伤害属性处理。
 * 属性值统一按百分比解释，例如 0.15 = 对应武器伤害 +15%。
 */
public class OneHandWeaponDamage extends Attribute {

    protected OneHandWeaponDamage(String descriptionId, double defaultValue) {
        super(descriptionId, defaultValue);
    }

    public static float handle(float oldDamage, ServerPlayer player) {
        if (hasWeapon(player, GeneHunterTags.ONE_HAND_WEAPON)) {
            return apply(oldDamage, player.getAttribute(GeneHunterAttributeInit.ONE_HAND_WEAPON_DAMAGE));
        }
        if (hasWeapon(player, GeneHunterTags.TWO_HAND_WEAPON)) {
            return apply(oldDamage, player.getAttribute(GeneHunterAttributeInit.TWO_HAND_WEAPON_DAMAGE));
        }
        if (hasWeapon(player, GeneHunterTags.POLEARM_WEAPON)) {
            return apply(oldDamage, player.getAttribute(GeneHunterAttributeInit.POLEARM_WEAPON_DAMAGE));
        }
        return oldDamage;
    }

    private static boolean hasWeapon(ServerPlayer player, TagKey<Item> tag) {
        return player.getMainHandItem().is(tag);
    }

    private static float apply(float oldDamage, AttributeInstance attribute) {
        if (attribute == null) {
            return oldDamage;
        }
        return (float) (oldDamage * (1.0D + attribute.getValue()));
    }

}
