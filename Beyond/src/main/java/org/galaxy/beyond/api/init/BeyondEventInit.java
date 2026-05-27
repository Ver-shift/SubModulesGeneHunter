package org.galaxy.beyond.api.init;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.rogue_event.*;

import java.util.function.Supplier;

/**
 * RogueEventType 注册中心。
 */
public class BeyondEventInit {

    public static void register(IEventBus eventBus) {}

    public static final Supplier<RogueEventType> MONSTER;
    public static final Supplier<RogueEventType> SHOP;
    public static final Supplier<RogueEventType> BOSS;
    public static final Supplier<RogueEventType> HEAL;
    public static final Supplier<RogueEventType> REWARD;

    static {
        MONSTER = register(MonsterEventType.ID, MonsterEventType::new);
        SHOP    = register(ShopEventType.ID,    ShopEventType::new);
        BOSS    = register(BossEventType.ID,    BossEventType::new);
        HEAL    = register(HealEventType.ID,    HealEventType::new);
        REWARD  = register(RewardEventType.ID,  RewardEventType::new);
    }

    public static RogueEventType get(ResourceLocation id) {
        return BeyondRegistries.ROGUE_EVENT_TYPE.get(registryKey(id));
    }

    // ---- internal ----

    private static ResourceLocation registryKey(ResourceLocation id) {
        String path = id.getPath();
        return ResourceLocation.parse(id.getNamespace() + ":" + path.substring(path.lastIndexOf('/') + 1));
    }

    public static Supplier<RogueEventType> register(ResourceLocation id, Supplier<RogueEventType> sup) {
        return BeyondRegistries.ROGUE_EVENT_TYPE_REGISTER.register(
                id.getPath().substring(id.getPath().lastIndexOf('/') + 1), sup);
    }
}
