package com.pz.beyond.api.datagen;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.datagen.tag.BeyondEntityTagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Beyond 模组 DataGen 入口。
 * <p>
 * 在 {@link GatherDataEvent} 中按依赖顺序注册各类 Provider。
 */
@EventBusSubscriber(modid = Beyond.MODID)
public class BeyondDataGenerators {

    private BeyondDataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var helper = event.getExistingFileHelper();
        var pack = generator.getPackOutput();

        // 实体类型标签（beyond:hostile 等）
        generator.addProvider(
                event.includeServer(),
                new BeyondEntityTagsProvider(pack, registries, helper)
        );
    }
}
