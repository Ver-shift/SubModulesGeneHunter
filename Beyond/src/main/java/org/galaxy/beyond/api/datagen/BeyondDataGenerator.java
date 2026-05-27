package org.galaxy.beyond.api.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.datagen.custom.BeyondProgressGen;

@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondDataGenerator {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var output = gen.getPackOutput();
        var lookup = event.getLookupProvider();

        gen.addProvider(event.includeServer(), new BeyondProgressGen(output, lookup));
    }
}
