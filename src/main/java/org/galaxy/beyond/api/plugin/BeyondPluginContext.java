package org.galaxy.beyond.api.plugin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.IBeyondManager;

/**
 * 插件上下文。
 * <p>
 * 这里集中暴露插件常用对象，避免插件实现到处直接依赖 Beyond 静态字段。
 */
public final class BeyondPluginContext {

    BeyondPluginContext() {
    }

    public IBeyondManager getManager() {
        return Beyond.MANAGER;
    }

    public MinecraftServer getServer() {
        return Beyond.SERVER;
    }

    public ServerLevel getOverworld() {
        return Beyond.OVERWORLD;
    }
}
