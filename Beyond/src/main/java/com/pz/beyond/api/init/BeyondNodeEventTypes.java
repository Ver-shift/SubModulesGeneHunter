package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.NodeEventType;
import com.pz.beyond.nodeTask.BossEvent;
import com.pz.beyond.nodeTask.HealEvent;
import com.pz.beyond.nodeTask.ShopEvent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

/**
 * 节点事件类型注册
 */
public class BeyondNodeEventTypes {

    public static final ResourceKey<Registry<NodeEventType>> NODE_EVENT_TYPE_REGISTRY_KEY =
        ResourceKey.createRegistryKey(Beyond.asResource("node_event_types"));

    public static final Registry<NodeEventType> NODE_EVENT_TYPE_REGISTRY = new RegistryBuilder<>(NODE_EVENT_TYPE_REGISTRY_KEY).create();

    public static final DeferredRegister<NodeEventType> NODE_EVENT_TYPES =
        DeferredRegister.create(NODE_EVENT_TYPE_REGISTRY_KEY, Beyond.MODID);

    public static final NodeEventType EMPTY = new NodeEventType(Beyond.asResource("empty")){
        @Override
        public void cast(Context context) {}
        @Override
        public Result canNextEvent(Context context) {return Result.defaulted();}
    };
    // Boss 事件
    public static final Supplier<NodeEventType> BOSS_EVENT = NODE_EVENT_TYPES.register(BossEvent.BOSS_EVENT.getPath(), BossEvent::new);
    public static final Supplier<NodeEventType> SHOP_EVENT = NODE_EVENT_TYPES.register(ShopEvent.SHOP.getPath(), ShopEvent::new);
    public static final Supplier<NodeEventType> HEAL_EVENT = NODE_EVENT_TYPES.register(HealEvent.HEAL_EVENT.getPath(), HealEvent::new);


    public static void registerRegistry(NewRegistryEvent event) {
        event.register(NODE_EVENT_TYPE_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        NODE_EVENT_TYPES.register(eventBus);
    }

    public static NodeEventType getById(ResourceLocation id) {
        if (id == null) {
            return EMPTY;
        }
        NodeEventType type = NODE_EVENT_TYPE_REGISTRY.get(id);
        return type == null ? EMPTY : type;
    }

    public static ResourceLocation getId(NodeEventType type) {
        return type == null ? EMPTY.getIdentifier() : type.getIdentifier();
    }
}
