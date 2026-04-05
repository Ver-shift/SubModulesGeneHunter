package org.biotech.api.init;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.biotech.Biotech;
import org.biotech.api.GeneData;

public class AttachInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GeneData>> GENE_DATA = ATTACHMENT_TYPES.register(
            "gene_data",
            () -> AttachmentType.builder((holder) -> {
                if (holder instanceof ServerPlayer player) {
                    return new GeneData(player);
                }
                return new GeneData();
            })
                    .serialize(GeneData.CODEC)
                    .sync(GeneData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

}
