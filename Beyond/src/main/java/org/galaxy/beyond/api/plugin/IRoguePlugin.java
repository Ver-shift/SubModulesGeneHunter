package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.galaxy.beyond.api.system.definition.ProgressDefinition;

/**
 * Beyond 肉鸽系统插件入口。
 * <p>
 * 插件可以在不同生命周期注册不同数据：
 * 注册表阶段注册 RogueCap / RogueEventType，资源包阶段注册默认关卡，
 * 服务器启动后再处理依赖 MinecraftServer 的逻辑。
 */
@SuppressWarnings("unused")
public interface IRoguePlugin {

    /**
     * 插件唯一 id。
     * <p>
     * Beyond 会用它去重，避免同一个插件被自动扫描和手动注册两次。
     */
    ResourceLocation getId();

    /**
     * Beyond 构造阶段调用。
     * <p>
     * 适合初始化插件自己的轻量缓存；此时服务器对象还不可用。
     */
    default void onConstruct(BeyondPluginContext context) {
    }

    /**
     * 注册 RogueCap。
     * <p>
     * 该方法会在 NeoForge 注册表冻结前调用，插件应只在这里注册代码 Cap。
     */
    default void registerRogueCaps(RogueCapRegistration registration) {
    }

    /**
     * 注册 RogueEventType。
     * <p>
     * 该方法会在 NeoForge 注册表冻结前调用，插件应只在这里注册代码事件类型。
     */
    default void registerRogueEvents(RogueEventRegistration registration) {
    }

    /**
     * 注册代码默认关卡。
     * <p>
     * 这里提供的是默认数据，资源包加载时同 id 的 JSON 会覆盖这里注册的关卡。
     */
    default void registerProgress(ProgressRegistration registration) {
    }

    /**
     * 初始化 RogueData 默认装载的 RogueCap。
     * <p>
     * 这里不是注册 RogueCap 类型，而是声明新 RogueData 创建时默认拥有哪几个 Cap。
     * 声明的 Cap 必须已经注册到 Beyond RogueCap 注册表，否则初始化会直接报错。
     */
    default void initRogueCaps(RogueCapInit registration) {
    }

    /**
     * 初始化当前关卡专属的 RogueCap。
     * <p>
     * 这里声明的 Cap 只会在指定 {@code progressId} 对应的 RogueData 初始化时加入。
     * 声明的 Cap 必须已经注册到 Beyond RogueCap 注册表。
     */
    default void initProgressCaps(ResourceLocation progressId, ProgressDefinition definition, RogueCapInit registration) {
    }

    /**
     * 资源包 reload 后调用。
     * <p>
     * 适合刷新依赖关卡定义、语言数据或其他资源包数据的缓存。
     */
    default void onReload(BeyondPluginContext context) {
    }

    /**
     * 服务器启动完成后调用。
     * <p>
     * 此时 {@link MinecraftServer}、主世界和 Beyond 全局数据已经可以安全访问。
     */
    default void onServerStarted(MinecraftServer server, BeyondPluginContext context) {
    }
}
