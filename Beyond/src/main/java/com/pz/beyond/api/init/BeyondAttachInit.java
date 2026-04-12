package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BeyondAttachInit {

    // 附件类型注册器
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beyond.MODID);

    /**
     * 注册到事件总线
     */
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

//    public DeferredHolder<AttachmentType<?>,AttachmentType<?>>
}
