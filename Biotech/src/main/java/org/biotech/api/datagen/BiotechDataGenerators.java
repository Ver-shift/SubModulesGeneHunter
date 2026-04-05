package org.biotech.api.datagen;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.biotech.Biotech;
import org.biotech.api.datagen.model.BiotechItemModelProvider;

@EventBusSubscriber(modid = Biotech.MODID)
public class BiotechDataGenerators {



    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var vanillaPack = generator.getVanillaPack(true);
        var helper = event.getExistingFileHelper();
        var pack = generator.getPackOutput();

        generator.addProvider(event.includeClient(),new BiotechItemModelProvider(pack,Biotech.MODID,helper));


    }
}
