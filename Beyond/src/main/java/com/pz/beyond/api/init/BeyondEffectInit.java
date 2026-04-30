package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.effect.SafeEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BeyondEffectInit {

    public static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(Registries.MOB_EFFECT, Beyond.MODID);

    public static final DeferredHolder<MobEffect, SafeEffect> SAFE_EFFECT =
        EFFECTS.register("safe", () -> new SafeEffect(MobEffectCategory.BENEFICIAL, 0x5FD37F));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
