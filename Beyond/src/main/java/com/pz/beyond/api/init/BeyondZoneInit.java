package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.zone.AbstractZone;
import com.pz.beyond.api.system.zone.zones.NodeZone;
import com.pz.beyond.api.system.zone.zones.PendingPlayerActiveZone;
import com.pz.beyond.api.system.zone.zones.PlayerActiveZone;
import com.pz.beyond.api.system.zone.zones.SafeZone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class BeyondZoneInit {

	private static final ResourceLocation EMPTY_ZONE_ID = ResourceLocation.withDefaultNamespace("empty");

	public static final ResourceKey<Registry<AbstractZone>> ZONE_REGISTRY_KEY =
		ResourceKey.createRegistryKey(Beyond.asResource("zone_registry"));

	public static final Registry<AbstractZone> ZONE_REGISTRY = new RegistryBuilder<>(ZONE_REGISTRY_KEY).create();

	public static final DeferredRegister<AbstractZone> ZONE =
		DeferredRegister.create(ZONE_REGISTRY_KEY, Beyond.MODID);

	public static final AbstractZone EMPTY = new AbstractZone(EMPTY_ZONE_ID) {
	};

	// 示例注册：后续可继续按此格式追加
	public static final Supplier<AbstractZone> SAFE_ZONE = ZONE.register("safe_zone", SafeZone::new);
	public static final Supplier<AbstractZone> PLAYER_ACTIVE_ZONE = ZONE.register("player_active_zone", PlayerActiveZone::new);
	public static final Supplier<AbstractZone> PENDING_PLAYER_ACTIVE_ZONE = ZONE.register("pending_player_active_zone", PendingPlayerActiveZone::new);
	public static final Supplier<AbstractZone> NODE_ZONE = ZONE.register("node_zone", NodeZone::new);

	public static void registerRegistry(NewRegistryEvent event) {
		event.register(ZONE_REGISTRY);
	}

	public static void register(IEventBus eventBus) {
		ZONE.register(eventBus);
	}

	public static Supplier<AbstractZone> registerZone(Supplier<? extends AbstractZone> sup) {
		return ZONE.register(sup.get().getIdentifier().getPath(), sup);
	}

	public static AbstractZone getZoneById(ResourceLocation zoneId) {
		if (zoneId == null) {
			return EMPTY;
		}
		AbstractZone zone = ZONE_REGISTRY.get(zoneId);
		return zone == null ? EMPTY : zone;
	}

	public static ResourceLocation getZoneId(AbstractZone zone) {
		return zone == null ? EMPTY.getIdentifier() : zone.getIdentifier();
	}
}
