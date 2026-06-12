package org.galaxy.beyond.api.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.datagen.custom.BeyondProgressGen;
import org.galaxy.beyond.api.datagen.custom.BeyondSpawnDefinitionGen;
import org.galaxy.beyond.api.datagen.data.BeyondItemModelProvider;

@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondDataGenerator {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var output = gen.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();

        var lookup = event.getLookupProvider();
        gen.addProvider(event.includeServer(), new BeyondProgressGen(output, lookup));
        gen.addProvider(event.includeServer(), new BeyondSpawnDefinitionGen(output));
        gen.addProvider(event.includeClient(), new BeyondItemModelProvider(output, existingFileHelper));
    }
}
