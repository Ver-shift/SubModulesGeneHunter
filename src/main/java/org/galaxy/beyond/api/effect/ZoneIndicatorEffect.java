package org.galaxy.beyond.api.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.zone.ZoneType;

public class ZoneIndicatorEffect extends MobEffect {

    public ZoneIndicatorEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3F78DC);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        ZoneType zone = BeyondAPI.getBeyondMobData(mob).getZoneType();
        int target = switch (zone) {
            case Safe_Zone -> 0;
            case Node_Zone -> 1;
            case Active_Zone -> 2;
            default -> 0;
        };
        if (amplification != target) {
            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getResourceKey(this)
                    .flatMap(BuiltInRegistries.MOB_EFFECT::get)
                    .orElseThrow();
            mob.removeEffect(holder);
            mob.addEffect(new MobEffectInstance(holder, -1, target, false, true, true));
        }
        return true;
    }
}
