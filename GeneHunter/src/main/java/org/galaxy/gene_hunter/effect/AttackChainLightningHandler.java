package org.galaxy.gene_hunter.effect;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.system.temperature.AttackTemperaturePayload;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = GeneHunter.MODID)
public final class AttackChainLightningHandler {
    /** 传导伤害是系统固定值，不作为可堆叠的玩家属性暴露给数据包。 */
    private static final double CHAIN_LIGHTNING_DAMAGE = 3.0D;
    private static final Map<UUID, List<ChainState>> ACTIVE_CHAINS = new HashMap<>();

    private AttackChainLightningHandler() {
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Post event) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || player.level().isClientSide()) {
            return;
        }

        double chance = AttackPropagationFactory.triggerChance(player, AttackPropagationFactory.Type.CHAIN_LIGHTNING);
        double damage = CHAIN_LIGHTNING_DAMAGE;
        if (chance <= 0.0D || damage <= 0.0D || player.getRandom().nextDouble() >= chance) {
            return;
        }

        int additionalChains = Mth.floor(attributeValue(player, GeneHunterAttributeInit.ATTACK_CHAIN_LIGHTNING_CHAIN_COUNT));
        int intervalTicks = Math.max(1, (int) Math.ceil(attributeValue(player, GeneHunterAttributeInit.ATTACK_CHAIN_LIGHTNING_PROPAGATION_INTERVAL)));
        double range = attributeValue(player, GeneHunterAttributeInit.ATTACK_CHAIN_LIGHTNING_RANGE);
        ChainState chain = new ChainState(event.getEntity(), damage, additionalChains, intervalTicks, range,
                player.level().getGameTime(), AttackTemperaturePayload.from(player));
        chain.zap(player, player, event.getEntity(), false);

        if (chain.remainingChains() > 0) {
            ACTIVE_CHAINS.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>()).add(chain);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        List<ChainState> chains = ACTIVE_CHAINS.get(player.getUUID());
        if (chains == null) {
            return;
        }

        long gameTime = player.level().getGameTime();
        chains.removeIf(chain -> !chain.propagate(player, gameTime));
        if (chains.isEmpty()) {
            ACTIVE_CHAINS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE_CHAINS.remove(event.getEntity().getUUID());
    }

    private static double attributeValue(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        var instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }

    private static final class ChainState {
        private final double damage;
        private final int intervalTicks;
        private final double range;
        private final AttackTemperaturePayload temperature;
        private final Set<UUID> visitedTargets = new HashSet<>();
        private LivingEntity currentTarget;
        private int remainingChains;
        private long nextPropagationTick;

        private ChainState(LivingEntity initialTarget, double damage, int remainingChains, int intervalTicks, double range,
                           long gameTime, AttackTemperaturePayload temperature) {
            this.currentTarget = initialTarget;
            this.damage = damage;
            this.remainingChains = remainingChains;
            this.intervalTicks = intervalTicks;
            this.range = range;
            this.nextPropagationTick = gameTime + intervalTicks;
            this.temperature = temperature;
            this.visitedTargets.add(initialTarget.getUUID());
        }

        private int remainingChains() {
            return remainingChains;
        }

        private boolean propagate(ServerPlayer player, long gameTime) {
            if (remainingChains <= 0 || currentTarget.isRemoved() || currentTarget.level() != player.level()) {
                return false;
            }
            if (gameTime < nextPropagationTick) {
                return true;
            }

            LivingEntity nextTarget = findNextTarget(player, currentTarget, visitedTargets, range);
            if (nextTarget == null) {
                return false;
            }

            zap(player, currentTarget, nextTarget, true);
            currentTarget = nextTarget;
            visitedTargets.add(nextTarget.getUUID());
            remainingChains--;
            nextPropagationTick = gameTime + intervalTicks;
            return remainingChains > 0;
        }

        private void zap(ServerPlayer player, Entity source, LivingEntity target, boolean applyTemperature) {
            Vec3 start = source.getBoundingBox().getCenter();
            Vec3 destination = target.getBoundingBox().getCenter();
            DamageSources.applyDamage(target, (float) damage,
                    SpellRegistry.CHAIN_LIGHTNING_SPELL.get().getDamageSource(player, player).setIFrames(0));
            if (applyTemperature && target instanceof net.minecraft.world.entity.Mob mob) {
                temperature.apply(mob);
            }
            MagicManager.spawnParticles(player.level(), ParticleHelper.ELECTRICITY,
                    target.getX(), target.getY() + target.getBbHeight() / 2.0D, target.getZ(),
                    10, target.getBbWidth() / 3.0D, target.getBbHeight() / 3.0D, target.getBbWidth() / 3.0D, 0.1D, false);
            player.serverLevel().sendParticles(new ZapParticleOption(destination),
                    start.x, start.y, start.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            if (source != player) {
                target.playSound(SoundRegistry.CHAIN_LIGHTNING_CHAIN.get(), 2.0F, 1.0F);
            }
        }

        private static LivingEntity findNextTarget(ServerPlayer player, LivingEntity source, Set<UUID> visitedTargets, double range) {
            return player.level().getEntitiesOfClass(LivingEntity.class, source.getBoundingBox().inflate(range), target ->
                            target != player
                                    && target.isAlive()
                                    && target.canBeHitByProjectile()
                                    && !visitedTargets.contains(target.getUUID())
                                    && !DamageSources.isFriendlyFireBetween(player, target)
                                    && target.distanceToSqr(source) < range * range
                                    && Utils.hasLineOfSight(player.level(), source.getEyePosition(), target.getEyePosition(), true))
                    .stream()
                    .min(Comparator.comparingDouble(target -> target.distanceToSqr(source)))
                    .orElse(null);
        }
    }
}
