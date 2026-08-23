package org.galaxy.gene_hunter.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.system.temperature.AttackTemperaturePayload;

@EventBusSubscriber(modid = GeneHunter.MODID)
public final class AttackTemperatureHandler {
    private AttackTemperatureHandler() {
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Post event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !(event.getEntity() instanceof Mob mob)
                || player.level().isClientSide()) {
            return;
        }

        AttackTemperaturePayload.from(player).apply(mob);
    }
}
