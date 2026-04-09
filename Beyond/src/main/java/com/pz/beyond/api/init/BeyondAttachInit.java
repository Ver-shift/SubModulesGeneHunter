package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.zone.AbstractZone;
import com.pz.beyond.api.system.zone.SafeZone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeyondAttachInit {

    // 附件类型注册器
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beyond.MODID);

    // SafeZone Level附件（存储在Level上，全局一个）
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SafeZone>> SAFE_ZONE =
HMENT_TYPES.register(
            "safe_zone",
            () -> AttachmentType.<SafeZone>builder(SafeZone::new)
                    .serialize(SafeZone.CODEC)
             SafeZone.IDENTIFIER.getPath(),
            () -> AttachmentType.<SafeZone>builder(SafeZone::new)
                    .serialize(SafeZone.CODEC)
                    .build()
    );

    public static AbstractZone get(ResourceLocation identifier) {
        ATTACHMENT_TYPES.getEntries().stream().map(attachmentTypeDeferredHolder -> {
            attachmentTypeDeferredHolder.getId().equals(identifier);
        })
    }
STRY = new ConcurrentHashMap<>();

    /**
     * 初始化 Zone 映射（在模组初始化时调用）
     */
    public static void initZoneRegistry() {
        registerZoneType(SafeZone.IDENTIFIER, SAFE_ZONE.get());
        // 未来添加其他 Zone 类型：
        // registerZoneType(NodeZone.IDENTIFIER, NODE_ZONE.get());
        // registerZoneType(ActivityZone.IDENTIFIER, ACTIVITY_ZONE.get());
    }

    private static void registerZoneType(ResourceLocation id, AttachmentType<? extends AbstractZone> type) {
        ZONE_REGISTRY.put(id, type);
    }

    /**
     * 根据 ID 获取 AttachmentType
     */
    public static AttachmentType<? extends AbstractZone> getAttachmentType(ResourceLocation identifier) {
        return ZONE_REGISTRY.get(identifier);
    }

    /**
     * 从 Level 根据 ID 获取 Zone 数据
     */
    public static AbstractZone getZone(Level level, ResourceLocation identifier) {
        AttachmentType<? extends AbstractZone> type = getAttachmentType(identifier);
        if (type == null || level == null) {
            return null;
        }
        return level.getData(type);
    }

    /**
     * 注册到事件总线
     */
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
