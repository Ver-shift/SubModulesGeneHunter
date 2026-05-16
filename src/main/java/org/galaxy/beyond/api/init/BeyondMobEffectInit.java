package org.galaxy.beyond.api.init;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.effect.ZoneIndicatorEffect;

public class BeyondMobEffectInit {

    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT, Beyond.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> ZONE_INDICATOR =
            MOB_EFFECTS.register("zone_indicator", ZoneIndicatorEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
