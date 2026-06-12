package org.galaxy.gene_hunter.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.datagen.tag.GeneHunterBlockTagProvider;
import org.galaxy.gene_hunter.datagen.tag.GeneHunterTagProvider;

@EventBusSubscriber(modid = GeneHunter.MODID)
public class GeneHunterDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var helper = event.getExistingFileHelper();
        var pack = generator.getPackOutput();

        // 注册方块标签提供者
        var blockTagProvider = generator.addProvider(
                event.includeServer(),
                new GeneHunterBlockTagProvider(pack, registries, helper)
        );

        // 注册物品标签提供者（依赖方块标签）
        generator.addProvider(
                event.includeServer(),
                new GeneHunterTagProvider(pack, registries, blockTagProvider.contentsGetter(), helper)
        );
        generator.addProvider(
                event.includeServer(),
                new GeneHunterProgressGen(pack, registries)
        );
        generator.addProvider(
                event.includeServer(),
                new GeneHunterSpawnDefinitionGen(pack)
        );
    }
}
