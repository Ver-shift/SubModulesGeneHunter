package com.pz.beyond.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置文件
 * <p>
 * 控制世界加载时的安全区初始化行为，包括村庄搜索、出生点设置、玩家传送等。
 */
public class ServerConfig {

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	/**
	 * 通用调试配置。
	 * <p>
	 * 嵌套静态类里使用静态字段 + 静态初始化块完成向 {@link #BUILDER} 的注册，
	 * 通过 {@code ServerConfig.Generic.DEBUG_MESSAGES} 访问。
	 */
	public static final class Generic {

		/** 是否输出 Beyond 模块的调试日志（由 {@code Beyond.debugLog} 读取）。 */
		public static final ModConfigSpec.BooleanValue DEBUG_MESSAGES;

		static {
			BUILDER.comment("Beyond 通用调试开关").push("generic");
			DEBUG_MESSAGES = BUILDER
					.comment("是否输出 Beyond 模块的调试日志。关闭后 Beyond.debugLog(...) 将被静默。")
					.define("debugMessages", false);
			BUILDER.pop();
		}

		private Generic() {
		}
	}

	public static final ModConfigSpec SPEC;

	static {
		// 触发 Generic 的静态初始化，把 DEBUG_MESSAGES 注册进 BUILDER
		try {
			Class.forName(Generic.class.getName());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Unable to load Beyond ServerConfig.Generic", e);
		}
		SPEC = BUILDER.build();
	}

	private ServerConfig() {
	}
}
