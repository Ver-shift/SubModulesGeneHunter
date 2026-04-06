package org.biotech.api.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.biotech.network.MergePacket;

@EventBusSubscriber
public class BiotechPacketInit {
    @SubscribeEvent // on the mod event bus
    public static void register(RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(MergePacket.TYPE, MergePacket.STREAM_CODEC, MergePacket::handle);
    }
}
