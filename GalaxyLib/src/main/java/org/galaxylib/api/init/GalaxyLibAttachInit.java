package org.galaxylib.api.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.world.level.Level;
import org.galaxylib.GalaxyLib;
import org.galaxylib.api.system.random.RandomManager;

public class GalaxyLibAttachInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GalaxyLib.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<RandomManager>> RANDOM_MANAGER = ATTACHMENT_TYPES.register(
            "random_manager",
            () -> AttachmentType.builder((holder) -> new RandomManager())
                    .serialize(RandomManager.CODEC)
                    .sync(RandomManager.STREAM_CODEC)
                    .build()
    );

    public static RandomManager getRandomManager(Level level) {
        return level.getData(RANDOM_MANAGER);
    }
}
