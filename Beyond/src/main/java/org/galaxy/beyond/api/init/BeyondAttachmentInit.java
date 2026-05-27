package org.galaxy.beyond.api.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondGlobalData;
import org.galaxy.beyond.api.system.BeyondMobData;
import org.galaxy.beyond.api.system.BeyondPlayerData;
import org.galaxy.beyond.api.system.large.BeyondLargeLevelData;
import org.galaxy.beyond.api.system.large.BeyondLargeLevelDataSyncHandler;

import java.util.function.Supplier;

public class BeyondAttachmentInit {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beyond.MODID);

    public static final Supplier<AttachmentType<BeyondGlobalData>> GLOBAL_DATA =
            ATTACHMENT_TYPES.register("global_data",
                    () -> AttachmentType.builder(BeyondGlobalData::new)
                            .serialize(BeyondGlobalData.CODEC.codec())
                            .sync(BeyondGlobalData.STREAM_CODEC)
                            .build());

    public static final Supplier<AttachmentType<BeyondPlayerData>> PLAYER_DATA =
            ATTACHMENT_TYPES.register("player_data",
                    () -> AttachmentType.builder(BeyondPlayerData::new)
                            .serialize(BeyondPlayerData.CODEC.codec())
                            .sync(BeyondPlayerData.STREAM_CODEC)
                            .build());

    public static final Supplier<AttachmentType<BeyondMobData>> MOB_DATA =
            ATTACHMENT_TYPES.register("mob_data",
                    () -> AttachmentType.builder(BeyondMobData::new)
                            .serialize(BeyondMobData.CODEC.codec())
                            .sync(BeyondMobData.STREAM_CODEC)
                            .build());

    public static final Supplier<AttachmentType<BeyondLargeLevelData>> LARGE_LEVEL_DATA =
            ATTACHMENT_TYPES.register("large_level_data",
                    () -> AttachmentType.builder(BeyondLargeLevelData::new)
                            .serialize(BeyondLargeLevelData.CODEC.codec())
                            .sync(new BeyondLargeLevelDataSyncHandler())
                            .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
