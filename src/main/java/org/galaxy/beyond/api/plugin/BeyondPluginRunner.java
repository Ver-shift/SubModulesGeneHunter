package org.galaxy.beyond.api.plugin;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;

import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Beyond 插件运行器。
 * <p>
 * 支持四种注册方式：自动发现插件类、手动加入插件实例、手动加入插件工厂、
 * 以及直接调用注册方法注册单项内容。
 */
public final class BeyondPluginRunner {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BeyondPluginContext CONTEXT = new BeyondPluginContext();
    private static final List<IRoguePlugin> PLUGINS = new ArrayList<>();
    private static final List<Supplier<? extends IRoguePlugin>> PLUGIN_SUPPLIERS = new ArrayList<>();
    private static final Set<Identifier> PLUGIN_IDS = new LinkedHashSet<>();
    private static final Map<Identifier, ProgressDefinition> MANUAL_PROGRESS = new LinkedHashMap<>();
    private static final Map<Identifier, ProgressDefinition> PLUGIN_PROGRESS = new LinkedHashMap<>();
    private static final List<Identifier> DEFAULT_CAP_IDS = new ArrayList<>();
    private static final RogueCapRegistration CAP_REGISTRATION = new RogueCapRegistration();
    private static final RogueEventRegistration EVENT_REGISTRATION = new RogueEventRegistration();

    private static boolean scanned;
    private static boolean constructed;
    private static boolean capsRegistered;
    private static boolean eventsRegistered;

    private BeyondPluginRunner() {
    }

    public static BeyondPluginContext context() {
        return CONTEXT;
    }

    public static void addPlugin(IRoguePlugin plugin) {
        addLoadedPlugin(plugin);
    }

    public static void addPlugin(Supplier<? extends IRoguePlugin> supplier) {
        if (supplier == null) return;
        PLUGIN_SUPPLIERS.add(supplier);
    }

    public static void registerCap(Supplier<? extends RogueCap> supplier) {
        if (supplier == null) return;
        CAP_REGISTRATION.addCap(supplier.get().getId(), supplier);
    }

    public static void registerDefaultCap(Identifier id) {
        if (id == null) return;
        DEFAULT_CAP_IDS.add(id);
    }

    public static List<Identifier> collectDefaultRogueCaps() {
        loadPlugins();
        RogueCapInit registration = new RogueCapInit();
        for (Identifier id : DEFAULT_CAP_IDS) {
            registration.initCap(id);
        }
        for (IRoguePlugin plugin : PLUGINS) {
            plugin.initRogueCaps(registration);
        }
        return registration.getCapIds();
    }

    public static void registerEvent(Identifier id, Supplier<? extends RogueEventType> supplier) {
        EVENT_REGISTRATION.addEvent(id, supplier);
    }

    public static void registerProgress(Identifier id, ProgressDefinition definition) {
        if (id == null || definition == null) return;
        MANUAL_PROGRESS.put(id, definition);
    }

    public static void loadPlugins() {
        if (!scanned) {
            scanned = true;
            scanAutoInitPlugins();
        }
        loadSupplierPlugins();
    }

    public static void runConstruct() {
        if (constructed) return;
        loadPlugins();
        constructed = true;
        for (IRoguePlugin plugin : PLUGINS) {
            plugin.onConstruct(CONTEXT);
        }
    }

    public static void registerRogueCaps() {
        if (capsRegistered) return;
        loadPlugins();
        capsRegistered = true;
        for (IRoguePlugin plugin : PLUGINS) {
            plugin.registerRogueCaps(CAP_REGISTRATION);
        }
    }

    public static void registerRogueEvents() {
        if (eventsRegistered) return;
        loadPlugins();
        eventsRegistered = true;
        for (IRoguePlugin plugin : PLUGINS) {
            plugin.registerRogueEvents(EVENT_REGISTRATION);
        }
    }

    public static Map<Identifier, ProgressDefinition> collectProgress() {
        loadPlugins();
        PLUGIN_PROGRESS.clear();
        ProgressRegistration registration = new ProgressRegistration();
        for (IRoguePlugin plugin : PLUGINS) {
            plugin.registerProgress(registration);
        }
        PLUGIN_PROGRESS.putAll(registration.getProgress());

        Map<Identifier, ProgressDefinition> progress = new LinkedHashMap<>();
        progress.putAll(MANUAL_PROGRESS);
        progress.putAll(PLUGIN_PROGRESS);
        return progress;
    }

    public static void onReload() {
        loadPlugins();
        for (IRoguePlugin plugin : PLUGINS) {
            plugin.onReload(CONTEXT);
        }
    }

    public static void onServerStarted(MinecraftServer server) {
        loadPlugins();
        for (IRoguePlugin plugin : PLUGINS) {
            plugin.onServerStarted(server, CONTEXT);
        }
    }

    private static void loadSupplierPlugins() {
        if (PLUGIN_SUPPLIERS.isEmpty()) return;
        List<Supplier<? extends IRoguePlugin>> suppliers = new ArrayList<>(PLUGIN_SUPPLIERS);
        PLUGIN_SUPPLIERS.clear();
        for (Supplier<? extends IRoguePlugin> supplier : suppliers) {
            try {
                addLoadedPlugin(supplier.get());
            } catch (RuntimeException e) {
                LOGGER.error("Failed to create Beyond plugin from supplier", e);
            }
        }
    }

    private static void scanAutoInitPlugins() {
        Type annotationType = Type.getType(AutoInit.class);
        Set<String> classNames = new LinkedHashSet<>();
        for (ModFileScanData scanData : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotation : scanData.getAnnotations()) {
                if (Objects.equals(annotation.annotationType(), annotationType)) {
                    String className = normalizeClassName(annotation.memberName());
                    if (className == null || className.isBlank()) {
                        className = normalizeClassName(annotation.clazz().getClassName());
                    }
                    if (className != null && !className.isBlank()) {
                        classNames.add(className);
                    }
                }
            }
        }

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className, false, BeyondPluginRunner.class.getClassLoader());
                Class<? extends IRoguePlugin> pluginClass = clazz.asSubclass(IRoguePlugin.class);
                Constructor<? extends IRoguePlugin> constructor = pluginClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                addLoadedPlugin(constructor.newInstance());
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.error("Failed to load Beyond plugin: {}", className, e);
            }
        }
        LOGGER.info("Beyond AutoInit scan found {} plugin classes", classNames.size());
    }

    private static void addLoadedPlugin(IRoguePlugin plugin) {
        if (plugin == null) return;
        Identifier id = plugin.getId();
        if (id == null) {
            LOGGER.error("Skipped Beyond plugin with null id: {}", plugin.getClass().getName());
            return;
        }
        if (!PLUGIN_IDS.add(id)) {
            LOGGER.warn("Skipped duplicate Beyond plugin id: {}", id);
            return;
        }
        PLUGINS.add(plugin);
        LOGGER.info("Loaded Beyond plugin: {}", id);
    }

    private static String normalizeClassName(String name) {
        if (name == null || name.isBlank()) return null;
        return name.replace('/', '.');
    }
}
