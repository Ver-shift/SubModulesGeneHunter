package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.progress.ProgressCatalog;
import com.pz.beyond.api.system.zone.LevelZoneData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class BeyondAttachInit {

    // 附件类型注册器
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beyond.MODID);

    /**
     * 允许挂载数据的维度白名单
     */
    private static final List<ResourceKey<Level>> ALLOWED_DIMENSIONS = List.of(
            Level.OVERWORLD
    );

    /**
     * 进度目录数据，挂载在 Level 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ProgressCatalog>> PROGRESS_CATALOG =
            ATTACHMENT_TYPES.register("progress_catalog", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerLevel serverLevel && isAllowedDimension(serverLevel)) {
                                    return new ProgressCatalog();
                                }
                                return new ProgressCatalog();
                            })
                            .serialize(ProgressCatalog.CODEC)
                            .sync(ProgressCatalog.STREAM_CODEC)
                            .build()
            );

    /**
     * 区域数据，挂载在 Level 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LevelZoneData>> LEVEL_ZONE_DATA =
            ATTACHMENT_TYPES.register("level_zone_data", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerLevel serverLevel && isAllowedDimension(serverLevel)) {
                                    return new LevelZoneData();
                                }
                                return new LevelZoneData();
                            })
                            .serialize(LevelZoneData.CODEC)
                            .sync(LevelZoneData.STREAM_CODEC)
                            .build()
            );

    /**
     * 判断维度是否在白名单中
     */
    public static boolean isAllowedDimension(Level level) {
        return ALLOWED_DIMENSIONS.contains(level.dimension());
    }



    /**
     * 注册到事件总线
     */
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
