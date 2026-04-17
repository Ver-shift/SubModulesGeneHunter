package com.pz.beyond.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置文件
 * <p>
 * 控制世界加载时的安全区初始化行为，包括村庄搜索、出生点设置、玩家传送等。
 */
public class ServerConfig {

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	public static final ModConfigSpec SPEC;

	/** 是否启用村庄引导（世界首次加载时搜索村庄并初始化安全区） */
	private static final ModConfigSpec.BooleanValue ENABLE_VILLAGE_BOOTSTRAP;

	/** 是否将世界出生点设置到找到的村庄位置 */
	private static final ModConfigSpec.BooleanValue SET_WORLD_SPAWN;

	/** 是否在初始化时将在线玩家传送到新出生点 */
	private static final ModConfigSpec.BooleanValue TELEPORT_PLAYERS_ON_LOAD;

	/** 村庄搜索半径（单位：区块） */
	private static final ModConfigSpec.IntValue VILLAGE_SEARCH_RADIUS_CHUNKS;

	/** 初始安全区大小（正方形边长，单位：区块） */
	private static final ModConfigSpec.IntValue INITIAL_SAFE_ZONE_SIZE_CHUNKS;

	/** 保证覆盖村庄的最小安全区大小（正方形边长，单位：区块） */
	private static final ModConfigSpec.IntValue MIN_VILLAGE_WRAP_SIZE_CHUNKS;

	static {
		BUILDER.comment("安全区引导配置 - 世界加载时自动搜索村庄并初始化出生点与安全区").push("safe_zone_bootstrap");

		ENABLE_VILLAGE_BOOTSTRAP = BUILDER
			.comment(
				"是否启用村庄引导功能。",
				"启用后，世界首次加载时会搜索最近的村庄，",
				"并在村庄位置初始化安全区和出生点。"
			)
			.define("enable_village_bootstrap", true);

		SET_WORLD_SPAWN = BUILDER
			.comment(
				"是否将世界默认出生点设置到找到的村庄位置。",
				"启用后，新玩家将在村庄附近出生。"
			)
			.define("set_world_spawn", true);

		TELEPORT_PLAYERS_ON_LOAD = BUILDER
			.comment(
				"是否在初始化时将当前在线玩家传送到新出生点。",
				"适用于首次创建世界或重置出生点的情况。"
			)
			.define("teleport_players_on_load", true);

		VILLAGE_SEARCH_RADIUS_CHUNKS = BUILDER
			.comment(
				"村庄搜索半径（单位：区块）。",
				"从默认出生点开始搜索最近村庄的最大距离。",
				"128 区块 = 2048 格方块"
			)
			.defineInRange("village_search_radius_chunks", 128, 1, 2048);

		INITIAL_SAFE_ZONE_SIZE_CHUNKS = BUILDER
			.comment(
				"初始安全区大小（正方形边长，单位：区块）。",
				"以村庄中心为基准的正方形安全区域。",
				"安全区内不会生成敌对生物。"
			)
			.defineInRange("initial_safe_zone_size_chunks", 5, 1, 4096);

		MIN_VILLAGE_WRAP_SIZE_CHUNKS = BUILDER
			.comment(
				"保证覆盖村庄的最小安全区大小（正方形边长，单位：区块）。",
				"实际安全区大小 = max(initial_safe_zone_size, min_village_wrap_size)",
				"确保村庄完全包含在安全区内。"
			)
			.defineInRange("min_village_wrap_size_chunks", 7, 1, 4096);

		BUILDER.pop();
		SPEC = BUILDER.build();
	}

	/** 私有构造，禁止实例化 */
	private ServerConfig() {
	}

	/** @return 是否启用村庄引导功能 */
	public static boolean enableVillageBootstrap() {
		return ENABLE_VILLAGE_BOOTSTRAP.get();
	}

	/** @return 是否将世界出生点设置到村庄位置 */
	public static boolean setWorldSpawn() {
		return SET_WORLD_SPAWN.get();
	}

	/** @return 是否在初始化时传送在线玩家 */
	public static boolean teleportPlayersOnLoad() {
		return TELEPORT_PLAYERS_ON_LOAD.get();
	}

	/** @return 村庄搜索半径（区块） */
	public static int villageSearchRadiusChunks() {
		return VILLAGE_SEARCH_RADIUS_CHUNKS.get();
	}

	/** @return 初始安全区大小（区块） */
	public static int initialSafeZoneSizeChunks() {
		return INITIAL_SAFE_ZONE_SIZE_CHUNKS.get();
	}

	/** @return 保证覆盖村庄的最小安全区大小（区块） */
	public static int minVillageWrapSizeChunks() {
		return MIN_VILLAGE_WRAP_SIZE_CHUNKS.get();
	}
}
