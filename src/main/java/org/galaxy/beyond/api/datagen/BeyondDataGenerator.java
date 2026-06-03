package org.galaxy.beyond.api.datagen;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.galaxy.beyond.api.datagen.custom.BeyondProgressGen;

public class BeyondDataGenerator {

    public static void register(IEventBus eventBus) {
        eventBus.addListener(BeyondDataGenerator::onGatherData);
    }

    public static void onGatherData(GatherDataEvent.Server event) {
        var gen = event.getGenerator();
        var output = gen.getPackOutput();

        var lookup = event.getLookupProvider();
        event.addProvider(new BeyondProgressGen(output, lookup));
    }
}
