package org.galaxy.gene_hunter.attribute;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.system.weapon.WeaponDamageType;
import org.galaxy.gene_hunter.api.system.weapon.WeaponDamageTypes;

public final class WeaponDamageHandler {

    private WeaponDamageHandler() {
    }

    public static float handle(float oldDamage, ServerPlayer player) {
        double gripDamage = 0.0D;
        double matchedShapeDamage = 0.0D;
        double convertedShapeDamage = 0.0D;
        double conversionRate = value(player.getAttribute(GeneHunterAttributeInit.WEAPON_DAMAGE_CONVERSION_RATE));

        for (WeaponDamageType type : WeaponDamageTypes.values()) {
            double damage = value(player.getAttribute(type.attribute()));
            if (type.channel() == WeaponDamageType.Channel.GRIP) {
                if (player.getMainHandItem().is(type.tag())) {
                    gripDamage += damage;
                }
                continue;
            }

            if (player.getMainHandItem().is(type.tag())) {
                matchedShapeDamage += damage;
            } else {
                convertedShapeDamage += damage * conversionRate;
            }
        }

        return (float) (oldDamage + gripDamage + matchedShapeDamage + convertedShapeDamage);
    }

    private static double value(AttributeInstance attribute) {
        if (attribute == null) {
            return 0.0D;
        }
        return attribute.getValue();
    }
}
