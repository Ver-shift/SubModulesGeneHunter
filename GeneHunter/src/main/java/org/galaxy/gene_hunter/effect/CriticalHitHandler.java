package org.galaxy.gene_hunter.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;

/** Applies the base 50% bonus damage for attribute-driven critical hits. */
@EventBusSubscriber(modid = GeneHunter.MODID)
public final class CriticalHitHandler {
    private static final double CRITICAL_DAMAGE_MULTIPLIER = 1.5D;

    private CriticalHitHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        var attribute = player.getAttribute(GeneHunterAttributeInit.CRITICAL_CHANCE.holder());
        double chance = attribute == null ? 0.0D : attribute.getValue();
        if (chance > 0.0D && player.getRandom().nextDouble() < chance) {
            var damageAttribute = player.getAttribute(GeneHunterAttributeInit.CRITICAL_DAMAGE_RATIO.holder());
            double bonus = damageAttribute == null ? 0.0D : damageAttribute.getValue();
            event.setNewDamage((float) (event.getNewDamage() * (CRITICAL_DAMAGE_MULTIPLIER + bonus)));
        }
    }
}
