package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.NodeColor;
import com.pz.beyond.progress.color.Blue;
import com.pz.beyond.progress.color.Green;
import com.pz.beyond.progress.color.Orange;
import com.pz.beyond.progress.color.Red;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

/**
 * 节点颜色注册
 */
public class BeyondNodeColors {

    public static final ResourceKey<Registry<NodeColor>> NODE_COLOR_REGISTRY_KEY =
        ResourceKey.createRegistryKey(Beyond.asResource("node_colors"));

    public static final Registry<NodeColor> NODE_COLOR_REGISTRY = new RegistryBuilder<>(NODE_COLOR_REGISTRY_KEY).create();

    public static final DeferredRegister<NodeColor> NODE_COLORS =
        DeferredRegister.create(NODE_COLOR_REGISTRY_KEY, Beyond.MODID);

    // 四种节点颜色
    public static final Supplier<NodeColor> GREEN = NODE_COLORS.register("green", Green::new);
    public static final Supplier<NodeColor> BLUE = NODE_COLORS.register("blue", Blue::new);
    public static final Supplier<NodeColor> ORANGE = NODE_COLORS.register("orange", Orange::new);
    public static final Supplier<NodeColor> RED = NODE_COLORS.register("red", Red::new);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(NODE_COLOR_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        NODE_COLORS.register(eventBus);
    }

    public static NodeColor getById(ResourceLocation id) {
        return NODE_COLOR_REGISTRY.get(id);
    }
}
