package org.galaxy.beyond.api.datagen;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.galaxy.beyond.api.datagen.custom.BeyondProgressGen;
import org.galaxy.beyond.api.datagen.data.BeyondModelProvider;

public class BeyondDataGenerator {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(BeyondDataGenerator::onGatherServerData);
        eventBus.addListener(BeyondDataGenerator::onGatherClientData);
    }

    public static void onGatherServerData(GatherDataEvent.Server event) {
        var gen = event.getGenerator();
        var output = gen.getPackOutput();

        var lookup = event.getLookupProvider();
        event.addProvider(new BeyondProgressGen(output, lookup));
    }

    public static void onGatherClientData(GatherDataEvent.Client event) {
        event.createProvider(BeyondModelProvider::new);
    }
}
