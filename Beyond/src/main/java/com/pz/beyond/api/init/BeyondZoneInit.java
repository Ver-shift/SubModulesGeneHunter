package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.ZoneType;
import com.pz.beyond.api.system.zone.zones.NodeZone;
import com.pz.beyond.api.system.zone.zones.PendingPlayerActiveZone;
import com.pz.beyond.api.system.zone.zones.PlayerActiveZone;
import com.pz.beyond.api.system.zone.zones.SafeZone;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;
import java.util.function.Supplier;

public class BeyondZoneInit {

	private static final ResourceLocation EMPTY_ZONE_ID = ResourceLocation.withDefaultNamespace("empty");

	public static final ResourceKey<Registry<ZoneType>> ZONE_REGISTRY_KEY =
		ResourceKey.createRegistryKey(Beyond.asResource("zone_registry"));

	public static final Registry<ZoneType> ZONE_REGISTRY = new RegistryBuilder<>(ZONE_REGISTRY_KEY).create();

	public static final DeferredRegister<ZoneType> ZONE =
		DeferredRegister.create(ZONE_REGISTRY_KEY, Beyond.MODID);

	public static final ZoneType EMPTY = new ZoneType(EMPTY_ZONE_ID) {
		@Override
		public void initialize(List<RuleData> listeners, ServerLevel level) {
			// 空区域，无需初始化
		}
	};

	// 示例注册：后续可继续按此格式追加
	public static final Supplier<ZoneType> SAFE_ZONE = ZONE.register("safe_zone", SafeZone::new);
	public static final Supplier<ZoneType> PLAYER_ACTIVE_ZONE = ZONE.register("player_active_zone", PlayerActiveZone::new);
	public static final Supplier<ZoneType> PENDING_PLAYER_ACTIVE_ZONE = ZONE.register("pending_player_active_zone", PendingPlayerActiveZone::new);
	public static final Supplier<ZoneType> NODE_ZONE = ZONE.register("node_zone", NodeZone::new);

	public static void registerRegistry(NewRegistryEvent event) {
		event.register(ZONE_REGISTRY);
	}

	public static void register(IEventBus eventBus) {
		ZONE.register(eventBus);
	}

	public static Supplier<ZoneType> registerZone(Supplier<? extends ZoneType> sup) {
		return ZONE.register(sup.get().getIdentifier().getPath(), sup);
	}

	public static ZoneType getZoneById(ResourceLocation zoneId) {
		if (zoneId == null) {
			return EMPTY;
		}
		ZoneType zone = ZONE_REGISTRY.get(zoneId);
		return zone == null ? EMPTY : zone;
	}

	public static ResourceLocation getZoneId(ZoneType zone) {
		return zone == null ? EMPTY.getIdentifier() : zone.getIdentifier();
	}
}
