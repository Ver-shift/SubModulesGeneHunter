package org.galaxy.beyond;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.*;
import org.galaxy.beyond.api.plugin.BeyondPluginRunner;
import org.galaxy.beyond.api.system.BeyondManager;
import org.galaxy.beyond.comand.BeyondCommand;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import org.galaxy.beyond.api.client.gui.BeyondConfigScreen;

/**
 * 关卡系统，在mc中呈现肉鸽玩法。
 */
@Mod(Beyond.MODID)
public class Beyond {
    public static final String MODID = "beyond";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static BeyondManager MANAGER;
    public static MinecraftServer SERVER;
    public static ServerLevel OVERWORLD;

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public Beyond(ModContainer modContainer, IEventBus modEventBus) {
        MANAGER = new BeyondManager();
        BeyondPluginRunner.runConstruct();

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BeyondConfigScreen.register(modContainer);
        }
        newRegistry(modEventBus);

        NeoForge.EVENT_BUS.addListener(BeyondCommand::register);
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
        modEventBus.addListener(BeyondRegistries::registerRegistries);
        BeyondRegistries.register(modEventBus);
        BeyondPluginRunner.registerRogueCaps();
        BeyondPluginRunner.registerRogueEvents();
        BeyondRogueCapInit.register(modEventBus);
        BeyondPhaseInit.register(modEventBus);
        BeyondEventInit.register(modEventBus);
        BeyondSpawnCharacterInit.register(modEventBus);
        BeyondComponentInit.register(modEventBus);
        BeyondBlockInit.register(modEventBus);
        BeyondItemInit.register(modEventBus);
        BeyondCreativeTabInit.register(modEventBus);
        BeyondAttachmentInit.register(modEventBus);
        BeyondMobEffectInit.register(modEventBus);
        BeyondStructureInit.register(modEventBus);
    }


}
