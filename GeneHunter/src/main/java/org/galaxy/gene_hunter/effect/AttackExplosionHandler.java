package org.galaxy.gene_hunter.effect;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.system.temperature.AttackTemperaturePayload;

@EventBusSubscriber(modid = GeneHunter.MODID)
public final class AttackExplosionHandler {
    private AttackExplosionHandler() {
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Post event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        double chance = AttackPropagationFactory.triggerChance(player, AttackPropagationFactory.Type.EXPLOSION);
        if (chance <= 0.0D || player.getRandom().nextDouble() >= chance || isOnCooldown(player)) {
            return;
        }

        double radius = attributeValue(player, GeneHunterAttributeInit.ATTACK_EXPLOSION_RADIUS);
        double damage = attributeValue(player, GeneHunterAttributeInit.ATTACK_EXPLOSION_DAMAGE);
        if (radius <= 0.0D || damage <= 0.0D) {
            return;
        }
        

        triggerExplosion(player, event.getEntity(), radius, damage, AttackTemperaturePayload.from(player));
        startCooldown(player, attributeValue(player, GeneHunterAttributeInit.ATTACK_EXPLOSION_COOLDOWN));
    }

    private static void triggerExplosion(ServerPlayer player, LivingEntity hitEntity, double radius, double damage,
                                         AttackTemperaturePayload temperature) {
        Vec3 origin = hitEntity.position();
        AABB area = new AABB(origin, origin).inflate(radius);

        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != player && entity.isAlive())) {
            double distance = target.position().distanceTo(origin);
            if (distance >= radius) {
                continue;
            }

            target.hurt(player.level().damageSources().source(DamageTypes.EXPLOSION, player), (float) damage);
            if (target instanceof net.minecraft.world.entity.Mob mob && target != hitEntity) {
                temperature.apply(mob);
            }
        }

        MagicManager.spawnParticles(player.level(), ParticleTypes.EXPLOSION,
                origin.x, origin.y, origin.z, 3, 0.1D, 0.1D, 0.1D, 0.3D, true);
        MagicManager.spawnParticles(player.level(), new BlastwaveParticleOptions(1.0F, 1.0F, 1.0F, (float) radius * 1.2F),
                origin.x, origin.y, origin.z, 1, 0.0D, 0.0D, 0.0D, 0.0D, true);
        player.level().playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 1.0F);
    }

    private static boolean isOnCooldown(ServerPlayer player) {
        return player.getData(GeneHunterAttachInit.ATTACK_EXPLOSION_COOLDOWN_END_TICK) > player.level().getGameTime();
    }

    private static void startCooldown(ServerPlayer player, double cooldownTicks) {
        long duration = Math.max(0L, Math.round(cooldownTicks));
        player.setData(GeneHunterAttachInit.ATTACK_EXPLOSION_COOLDOWN_END_TICK, player.level().getGameTime() + duration);
    }

    private static double attributeValue(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        var instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }
}
