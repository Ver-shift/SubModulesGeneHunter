package org.galaxy.beyond.api.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.galaxy.beyond.Beyond;

@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondDataGenerator {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        var gen = event.getGenerator();
        var output = gen.getPackOutput();
        var lookup = event.getLookupProvider();

        gen.addProvider(true, new BeyondProgressGen(output, lookup));
        gen.addProvider(true, new BeyondBlockStateProvider(output));
        gen.addProvider(true, new BeyondItemModelProvider(output));
    }
}
