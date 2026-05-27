package org.galaxy.beyond.api.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.datagen.custom.BeyondBiomeTagsProvider;
import org.galaxy.beyond.api.datagen.custom.BeyondProgressGen;
import org.galaxy.beyond.api.datagen.custom.BeyondStructureTagsProvider;
import org.galaxy.beyond.api.datagen.custom.BeyondWorldgenProvider;

@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondDataGenerator {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var output = gen.getPackOutput();

        event.createDatapackRegistryObjects(BeyondWorldgenProvider.BUILDER);
        var lookup = event.getLookupProvider();
        gen.addProvider(event.includeServer(), new BeyondProgressGen(output, lookup));
        gen.addProvider(event.includeServer(), new BeyondBiomeTagsProvider(output, lookup, event.getExistingFileHelper()));
        gen.addProvider(event.includeServer(), new BeyondStructureTagsProvider(output, lookup, event.getExistingFileHelper()));
    }
}
