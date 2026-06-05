package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

/**
 * Beyond 肉鸽系统插件入口。
 * <p>
 * 插件可以在不同生命周期注册不同数据：
 * 注册表阶段注册 RogueCap / RogueEventType，资源包阶段注册默认关卡，
 * 服务器启动后再处理依赖 MinecraftServer 的逻辑。
 */
public interface IRoguePlugin {

    Identifier getId();

    default void onConstruct(BeyondPluginContext context) {
    }

    default void registerRogueCaps(RogueCapRegistration registration) {
    }

    default void registerRogueEvents(RogueEventRegistration registration) {
    }

    default void registerProgress(ProgressRegistration registration) {
    }

    default void initRogueCaps(RogueCapInit registration) {
    }

    default void onReload(BeyondPluginContext context) {
    }

    default void onServerStarted(MinecraftServer server, BeyondPluginContext context) {
    }
}
