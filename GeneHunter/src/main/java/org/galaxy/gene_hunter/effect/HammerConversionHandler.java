package org.galaxy.gene_hunter.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;

/** Converts attack speed, critical chance, and reach into hammer damage. */
@EventBusSubscriber(modid = GeneHunter.MODID)
public final class HammerConversionHandler {
    private HammerConversionHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()
                || !player.getMainHandItem().is(GeneHunterTags.HAMMER_WEAPON)) {
            return;
        }

        double fixedDamage = percent(player, GeneHunterAttributeInit.CRITICAL_CHANCE)
                * value(player, GeneHunterAttributeInit.HAMMER_CRITICAL_CHANCE_FIXED_DAMAGE_PER_PERCENT);
        double damageRatio = attackSpeedBonusPercent(player)
                * value(player, GeneHunterAttributeInit.HAMMER_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT)
                + extraReach(player)
                * value(player, GeneHunterAttributeInit.HAMMER_RANGE_DAMAGE_RATIO_PER_POINT);

        if (fixedDamage > 0.0D || damageRatio > 0.0D) {
            event.setNewDamage((float) ((event.getNewDamage() + fixedDamage) * (1.0D + damageRatio / 100.0D)));
        }
    }

    private static double attackSpeedBonusPercent(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attribute == null || attribute.getBaseValue() <= 0.0D) return 0.0D;
        return Math.max(0.0D, (attribute.getValue() / attribute.getBaseValue() - 1.0D) * 100.0D);
    }

    private static double extraReach(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attribute == null) return 0.0D;
        return Math.max(0.0D, attribute.getValue() - attribute.getBaseValue());
    }

    private static double percent(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        return value(player, attribute) * 100.0D;
    }

    private static double value(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        AttributeInstance instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }
}
