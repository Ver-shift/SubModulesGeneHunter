package org.galaxy.gene_hunter.attribute;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.system.weapon.WeaponDamageType;
import org.galaxy.gene_hunter.api.system.weapon.WeaponDamageTypes;

public final class WeaponDamageHandler {

    private WeaponDamageHandler() {
    }

    public static float handle(float oldDamage, ServerPlayer player) {
        double matchedShapeDamage = 0.0D;
        double convertedShapeDamage = 0.0D;
        double matchedShapeMultiplier = 1.0D;
        double conversionRate = value(player.getAttribute(GeneHunterAttributeInit.WEAPON_DAMAGE_CONVERSION_RATE.holder()));

        for (WeaponDamageType type : WeaponDamageTypes.values()) {
            WeaponDamage damage = weaponDamage(
                    player.getAttribute(type.flatAttribute().holder()),
                    value(player.getAttribute(type.rateAttribute().holder()))
            );
            if (player.getMainHandItem().is(type.tag())) {
                matchedShapeDamage += damage.flat();
                matchedShapeMultiplier *= damage.multiplier();
            } else {
                convertedShapeDamage += damage.flat() * conversionRate;
            }
        }

        return (float) ((oldDamage + matchedShapeDamage) * matchedShapeMultiplier + convertedShapeDamage);
    }

    /**
     * Weapon-damage attributes have a zero base value, so their percentage modifiers
     * must be applied against the attack damage supplied to {@link #handle}.
     */
    private static WeaponDamage weaponDamage(AttributeInstance attribute, double rate) {
        if (attribute == null) {
            return new WeaponDamage(0.0D, 1.0D + rate / 100.0D);
        }

        double flat = 0.0D;
        double percentageBonus = 0.0D;
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                flat += modifier.amount();
            } else if (modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                percentageBonus += modifier.amount();
            }
        }
        return new WeaponDamage(flat, 1.0D + percentageBonus + rate / 100.0D);
    }

    private static double value(AttributeInstance attribute) {
        if (attribute == null) {
            return 0.0D;
        }
        return attribute.getValue();
    }

    private record WeaponDamage(double flat, double multiplier) {
        private static final WeaponDamage NONE = new WeaponDamage(0.0D, 1.0D);
    }
}
