package org.galaxy.beyond.api.init;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

import java.util.function.Supplier;

/**
 * Phase / EventType 注册入口。
 * <p>
 * Phase 已改为枚举（{@code PlayerPhase / NodePhase / RoguePhase}），无需 Supplier 注册。
 * 仅保留 RogueEventType 的动态注册能力。
 */
public class BeyondPhaseInit {

    public static void register(IEventBus eventBus) {}

    // ============================================================
    // Rogue EventType
    // ============================================================

    public static RogueEventType getRogueEventType(Identifier id) {
        return BeyondRegistries.ROGUE_EVENT_TYPE.get(registryKey(id)).map(h -> h.value()).orElse(null);
    }

    // ============================================================
    // internal
    // ============================================================

    private static Identifier registryKey(Identifier id) {
        String path = id.getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        return Identifier.parse(id.getNamespace() + ":" + name);
    }

    public static Supplier<RogueEventType> registerRogueEventType(Identifier id, Supplier<RogueEventType> sup) {
        return BeyondRegistries.ROGUE_EVENT_TYPE_REGISTER.register(
                id.getPath().substring(id.getPath().lastIndexOf('/') + 1), sup);
    }
}
