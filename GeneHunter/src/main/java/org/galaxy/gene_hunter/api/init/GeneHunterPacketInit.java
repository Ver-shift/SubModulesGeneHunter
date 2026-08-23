package org.galaxy.gene_hunter.api.init;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.galaxy.gene_hunter.network.PlayerSwingPayload;

public final class GeneHunterPacketInit {
    private GeneHunterPacketInit() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(PlayerSwingPayload.TYPE, PlayerSwingPayload.STREAM_CODEC, PlayerSwingPayload::handle);
    }
}
