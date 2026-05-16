package org.galaxy.beyond.api.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondGlobalData;
import org.galaxy.beyond.api.BeyondPlayerData;
import org.galaxy.beyond.api.network.FullSyncAttachmentHandler;

import java.util.function.Supplier;

public class BeyondAttachmentInit {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beyond.MODID);

    public static final Supplier<AttachmentType<BeyondGlobalData>> GLOBAL_DATA =
            ATTACHMENT_TYPES.register("global_data",
                    () -> AttachmentType.builder(BeyondGlobalData::new)
                            .serialize(BeyondGlobalData.CODEC)
                            .sync(new FullSyncAttachmentHandler<>(BeyondGlobalData.STREAM_CODEC, BeyondGlobalData::new))
                            .build());

    public static final Supplier<AttachmentType<BeyondPlayerData>> PLAYER_DATA =
            ATTACHMENT_TYPES.register("player_data",
                    () -> AttachmentType.builder(BeyondPlayerData::new)
                            .serialize(BeyondPlayerData.CODEC)
                            .sync(new FullSyncAttachmentHandler<>(BeyondPlayerData.STREAM_CODEC, BeyondPlayerData::new))
                            .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
