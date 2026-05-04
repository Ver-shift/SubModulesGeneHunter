package org.galaxy.beyond;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.BeyondRogueEventTypeInit;
import org.galaxy.beyond.api.init.BeyondZoneNodeCapInit;
import org.galaxy.beyond.api.system.BeyondManager;
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
    public static final String MODID = "beyond";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static BeyondManager MANAGER;

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public Beyond(ModContainer modContainer, IEventBus modEventBus) {
        MANAGER = new BeyondManager();

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        newRegistry(modEventBus);

    }

    /**
     * 仅在 debug 模式（CommonConfig.debugMessages = true）下输出调试信息。
     */
    public static void debugInfo(String key, Object... args) {
        if (CommonConfig.DEBUG_MODE.get()) {
            LOGGER.info(key, args);
        }
    }

    public void newRegistry(IEventBus modEventBus) {
        modEventBus.addListener(BeyondRogueEventTypeInit::registerRegistry);
        modEventBus.addListener(BeyondZoneNodeCapInit::registerRegistry);
        BeyondRogueEventTypeInit.register(modEventBus);
        BeyondZoneNodeCapInit.register(modEventBus);
    }


}
