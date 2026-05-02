package org.galaxy.beyond.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 通用模组配置文件（COMMON 类型，客户端和服务端均生效）。
 */
public class CommonConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 是否输出 Beyond 模块的调试信息 */
    public static final ModConfigSpec.BooleanValue DEBUG_MODE;

    static {
        BUILDER.comment("Beyond 通用调试开关").push("generic");
        DEBUG_MODE = BUILDER
                .comment("是否输出 Beyond 模块的调试日志。关闭后 debugInfo() 将被静默。")
                .define("debugMessages", false);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CommonConfig() {
    }
}
