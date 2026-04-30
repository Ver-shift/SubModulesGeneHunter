package com.pz.beyond.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class SafeEffect extends MobEffect {
    private static final int SAFE_BUFF_INTERVAL_TICKS = 23 * 10;
    private static final int SAFE_BUFF_DURATION_TICKS = 23 * 10;

    public SafeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            // Clear exhaustion to stop hunger drain while in safe zone.
            player.getFoodData().setExhaustion(0.0F);

            if (player.tickCount % SAFE_BUFF_INTERVAL_TICKS == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, SAFE_BUFF_DURATION_TICKS, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, SAFE_BUFF_DURATION_TICKS, 0, true, false, true));
            }
        }
        return true;
    }
}
