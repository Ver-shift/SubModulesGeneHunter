package org.galaxy.beyond.api.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.galaxy.beyond.api.config.CommonConfig;
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
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        if (!CommonConfig.isRogueDimension(mob.level())) {
            mob.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));
            return true;
        }

        ZoneType zone = BeyondAPI.getBeyondMobData(mob).getZoneType();
        if (amplification == 4 && mob.tickCount % 20 == 0) {
            float health = mob.getHealth();
            if (health > 2.0F) {
                mob.setHealth(Math.max(2.0F, health * 0.9F));
            }
            if (mob instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("beyond.effect.zone_indicator.outside_warning"));
            }
        }
        int target = switch (zone) {
            case Safe_Zone -> 0;
            case Node_Zone -> 1;
            case Active_Zone -> 2;
            case Empty -> 4;
        };
        if (amplification != target) {
            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this);
            boolean visible = CommonConfig.DEBUG_MODE.get();
            mob.removeEffect(holder);
            mob.addEffect(new MobEffectInstance(holder, -1, target, false, visible, visible));
        }
        return true;
    }
}
