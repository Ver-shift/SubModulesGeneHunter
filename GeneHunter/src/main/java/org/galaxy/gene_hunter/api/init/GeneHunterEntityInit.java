package org.galaxy.gene_hunter.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.effect.SlashWaveEntity;

public final class GeneHunterEntityInit {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, GeneHunter.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<SlashWaveEntity>> SLASH_WAVE =
            ENTITY_TYPES.register("slash_wave", () -> EntityType.Builder.<SlashWaveEntity>of(SlashWaveEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(GeneHunter.asResource("slash_wave").toString()));

    private GeneHunterEntityInit() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
