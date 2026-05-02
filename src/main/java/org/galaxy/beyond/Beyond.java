package org.galaxy.beyond;

import org.galaxy.beyond.api.config.CommonConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

/**
 * 关卡系统，在mc中呈现肉鸽玩法。
 */
@Mod(Beyond.MODID)
public class Beyond {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "beyond";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Beyond(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    /**
     * 仅在 debug 模式（CommonConfig.debugMessages = true）下输出调试信息。
     */
    public static void debugInfo(String key, Object... args) {
        if (CommonConfig.DEBUG_MODE.get()) {
            LOGGER.info(key, args);
        }
    }


}
