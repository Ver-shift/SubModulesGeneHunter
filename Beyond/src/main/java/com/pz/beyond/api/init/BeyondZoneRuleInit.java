package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.rule.IZoneRule;
import com.pz.beyond.rule.NoEat;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class BeyondZoneRuleInit {
    public static final ResourceKey<Registry<IZoneRule>> RULE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Beyond.asResource("rule_registry"));
    public static final Registry<IZoneRule> RULE_REGISTRY = new RegistryBuilder<>(RULE_REGISTRY_KEY).create();
    public static final DeferredRegister<IZoneRule> RULE =
            DeferredRegister.create(RULE_REGISTRY_KEY, Beyond.MODID);
    public static void registerRegistry(NewRegistryEvent event) {
        event.register(RULE_REGISTRY);
    }
    public static void register(IEventBus eventBus) {
        RULE.register(eventBus);
    }

    public static IZoneRule EMPTY = new IZoneRule() {
        @Override
        public ResourceLocation getIdentifier() {
            return Beyond.asResource("empty");
        }
    };

    public static Supplier<IZoneRule> registerRule(Supplier<IZoneRule> supplier) {
        return RULE.register(supplier.get().getIdentifier().getPath(), supplier);
    }

    public static final Supplier<IZoneRule> NO_EAT;

    static {
        NO_EAT = registerRule(NoEat::new);
    }


}
