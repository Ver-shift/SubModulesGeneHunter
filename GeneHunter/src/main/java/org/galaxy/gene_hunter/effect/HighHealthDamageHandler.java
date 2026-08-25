package org.galaxy.gene_hunter.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;

/** Applies the high-health damage bonus as a temporary attack-damage modifier. */
@EventBusSubscriber(modid = GeneHunter.MODID)
public final class HighHealthDamageHandler {
    private static final ResourceLocation HIGH_HEALTH_DAMAGE_ID = GeneHunter.asResource("high_health_damage");

    private HighHealthDamageHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }

        var attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null) {
            return;
        }

        double threshold = attributeValue(player, GeneHunterAttributeInit.HIGH_HEALTH_DAMAGE_THRESHOLD);
        double damageRatio = attributeValue(player, GeneHunterAttributeInit.HIGH_HEALTH_DAMAGE_RATIO);
        boolean active = threshold > 0.0D
                && damageRatio != 0.0D
                && player.getHealth() / player.getMaxHealth() >= threshold;

        if (!active) {
            attackDamage.removeModifier(HIGH_HEALTH_DAMAGE_ID);
            return;
        }

        AttributeModifier currentModifier = attackDamage.getModifier(HIGH_HEALTH_DAMAGE_ID);
        if (currentModifier == null || currentModifier.amount() != damageRatio) {
            attackDamage.removeModifier(HIGH_HEALTH_DAMAGE_ID);
            attackDamage.addTransientModifier(new AttributeModifier(
                    HIGH_HEALTH_DAMAGE_ID,
                    damageRatio,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    private static double attributeValue(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        var instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }
}
