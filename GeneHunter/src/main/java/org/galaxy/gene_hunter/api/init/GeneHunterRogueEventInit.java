package org.galaxy.gene_hunter.api.init;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.beyond.api.init.BeyondRegistries;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.rogue_event.RogueRewardEventType;

import java.util.function.Supplier;

public class GeneHunterRogueEventInit {

    private static final DeferredRegister<RogueEventType> REGISTRAR =
            DeferredRegister.create(BeyondRegistries.Keys.ROGUE_EVENT_TYPE, GeneHunter.MODID);

    public static final Supplier<RogueEventType> ROGUE_REWARD;

    static {
        ROGUE_REWARD = register(RogueRewardEventType.ID, RogueRewardEventType::new);
    }

    public static void register(IEventBus eventBus) {
        REGISTRAR.register(eventBus);
    }

    private static Supplier<RogueEventType> register(ResourceLocation id, Supplier<RogueEventType> supplier) {
        return REGISTRAR.register(id.getPath(), supplier);
    }
}
