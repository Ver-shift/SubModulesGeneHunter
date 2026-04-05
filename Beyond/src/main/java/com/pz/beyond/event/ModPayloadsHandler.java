package com.pz.beyond.event;

import com.pz.beyond.data.SafeZoneClientCache;
import com.pz.beyond.network.SafeZonePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class ModPayloadsHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SafeZonePayload.TYPE,
                SafeZonePayload.STREAM_CODEC,
                ClientPayloadHandler::handleDataOnMain
        );
    }


    private static class ClientPayloadHandler {
        public static void handleDataOnMain(final SafeZonePayload data, final IPayloadContext context) {
            context.enqueueWork(()->{
                SafeZoneClientCache.update(data.centerX(),data.centerZ(),data.radius());
            });
        }
    }

}
