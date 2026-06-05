package org.galaxy.beyond.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.structure.terrain.TerrainBlendJigsawStructure;

public final class BeyondStructureInit {

    private static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Beyond.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<TerrainBlendJigsawStructure>> TERRAIN_BLEND_JIGSAW =
            STRUCTURE_TYPES.register("terrain_blend_jigsaw", () -> () -> TerrainBlendJigsawStructure.CODEC);

    private BeyondStructureInit() {
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}
