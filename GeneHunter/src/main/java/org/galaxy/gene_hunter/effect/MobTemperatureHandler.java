package org.galaxy.gene_hunter.effect;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;
import org.galaxy.gene_hunter.api.system.temperature.MobTemperature;
import org.galaxy.gene_hunter.api.system.temperature.MobTemperatureData;

@EventBusSubscriber(modid = GeneHunter.MODID)
public final class MobTemperatureHandler {
    private static final ResourceLocation COLD_SLOWDOWN_ID = GeneHunter.asResource("mob_temperature_cold_slowdown");
    private MobTemperatureHandler() {
    }

    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }

        MobTemperatureData temperature = mob.getData(GeneHunterAttachInit.MOB_TEMPERATURE);
        updateColdSlowdown(mob, temperature.coldSlowdown());
        spawnTemperatureParticles(mob, temperature);

        if (temperature.temperature() > 0) {
            if (mob.tickCount % 20 == 0) {
                mob.hurt(mob.level().damageSources().onFire(), (float) temperature.burnDamage());
            }
            MobTemperature.add(mob, -(int) Math.round(temperature.coolingRate()));
        } else if (temperature.temperature() < 0) {
            MobTemperature.add(mob, (int) Math.round(temperature.recoveryRate()));
        }

        MobTemperatureData updatedTemperature = mob.getData(GeneHunterAttachInit.MOB_TEMPERATURE);
        if (updatedTemperature.temperature() == 0) {
            updatedTemperature.clearEffect();
            mob.setData(GeneHunterAttachInit.MOB_TEMPERATURE, updatedTemperature);
        }
        updateColdSlowdown(mob, updatedTemperature.temperature() < 0 ? updatedTemperature.coldSlowdown() : 0.0D);
    }

    private static void spawnTemperatureParticles(Mob mob, MobTemperatureData temperature) {
        if (mob.tickCount % 4 != 0 || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        if (temperature.temperature() > 0) {
            level.sendParticles(ParticleTypes.FLAME,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                    4, mob.getBbWidth() * 0.35D, mob.getBbHeight() * 0.45D, mob.getBbWidth() * 0.35D, 0.01D);
            level.sendParticles(ParticleTypes.SMOKE,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                    2, mob.getBbWidth() * 0.35D, mob.getBbHeight() * 0.45D, mob.getBbWidth() * 0.35D, 0.01D);
        } else if (temperature.temperature() < 0) {
            MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                    3, mob.getBbWidth() * 0.5D, mob.getBbHeight() * 0.5D, mob.getBbWidth() * 0.5D, 0.03D, false);
            if (mob.tickCount % 40 == 0) {
                MagicManager.spawnParticles(level, ParticleHelper.ICY_FOG,
                        mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                        1, mob.getBbWidth() * 0.25D, mob.getBbHeight() * 0.25D, mob.getBbWidth() * 0.25D, 0.02D, true);
            }
        }
    }

    private static void updateColdSlowdown(Mob mob, double slowdown) {
        var movementSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        if (slowdown <= 0.0D) {
            if (movementSpeed.hasModifier(COLD_SLOWDOWN_ID)) {
                movementSpeed.removeModifier(COLD_SLOWDOWN_ID);
            }
            return;
        }

        AttributeModifier currentModifier = movementSpeed.getModifier(COLD_SLOWDOWN_ID);
        if (currentModifier == null || currentModifier.amount() != -slowdown) {
            movementSpeed.removeModifier(COLD_SLOWDOWN_ID);
            movementSpeed.addTransientModifier(new AttributeModifier(
                    COLD_SLOWDOWN_ID,
                    -slowdown,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }
}
