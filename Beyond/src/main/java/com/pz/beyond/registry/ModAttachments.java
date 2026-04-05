package com.pz.beyond.registry;

import com.pz.beyond.Beyond;
import com.pz.beyond.data.PlayerStateData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beyond.MODID);

    public static final Supplier<AttachmentType<PlayerStateData>> PLAYER_STATE = ATTACHMENT_TYPES.register("player_state",()->
            AttachmentType.<PlayerStateData>builder((Supplier<PlayerStateData>)PlayerStateData::new)
                    .serialize(PlayerStateData.CODEC)
                    .copyOnDeath()
                    .build());
}
