package org.galaxy.gene_hunter.api.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.galaxy.gene_hunter.command.GeneHunterCommand;

@EventBusSubscriber
public class GeneHunterCommandInit {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GeneHunterCommand.register(event.getDispatcher());
    }
}
