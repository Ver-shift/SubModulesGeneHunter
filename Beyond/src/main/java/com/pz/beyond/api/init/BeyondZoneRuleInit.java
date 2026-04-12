package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.rule.AbstractRule;
import com.pz.beyond.rule.AllSafeRule;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class BeyondZoneRuleInit {

	private static final ResourceLocation EMPTY_RULE_ID = ResourceLocation.withDefaultNamespace("empty");

	public static final ResourceKey<Registry<AbstractRule>> RULE_REGISTRY_KEY =
		ResourceKey.createRegistryKey(Beyond.asResource("rule_registry"));

	public static final Registry<AbstractRule> RULE_REGISTRY = new RegistryBuilder<>(RULE_REGISTRY_KEY).create();

	public static final DeferredRegister<AbstractRule> RULE =
		DeferredRegister.create(RULE_REGISTRY_KEY, Beyond.MODID);

	public static final AbstractRule EMPTY = new AbstractRule(EMPTY_RULE_ID) {
		@Override
		protected int getRuleValue() {
			return 0;
		}

		@Override
		protected RuleType getRuleType() {
			return RuleType.Natural;
		}

		@Override
		protected int getMaxLevel() {
			return 1;
		}
	};

	// 示例注册：可按这个格式继续扩展更多规则
	public static final Supplier<AbstractRule> ALL_SAFE_RULE =
		RULE.register("all_safe", () -> new AllSafeRule(Beyond.asResource("all_safe")));

	public static void registerRegistry(NewRegistryEvent event) {
		event.register(RULE_REGISTRY);
	}

	public static void register(IEventBus eventBus) {
		RULE.register(eventBus);
	}

	public static Supplier<AbstractRule> registerRule(Supplier<? extends AbstractRule> sup) {
		return RULE.register(sup.get().getIdentifier().getPath(), sup);
	}

	public static AbstractRule getRuleById(ResourceLocation ruleId) {
		if (ruleId == null) {
			return EMPTY;
		}
		AbstractRule rule = RULE_REGISTRY.get(ruleId);
		return rule == null ? EMPTY : rule;
	}

	public static ResourceLocation getRuleId(AbstractRule rule) {
		return rule == null ? EMPTY.getIdentifier() : rule.getIdentifier();
	}


}
