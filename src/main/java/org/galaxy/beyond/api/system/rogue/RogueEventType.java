package org.galaxy.beyond.api.system.rogue;

import lombok.NonNull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

/**
 * 事件类型
 */
public abstract class RogueEventType {
    private final Identifier id;

    public RogueEventType(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    /** 从 id 生成翻译键，如 beyond:monster → beyond.event.monster */
    public Component getDisplayName() {
        String key = id.getNamespace() + ".event." + id.getPath();
        return Component.translatable(key);
    }

    public abstract void cast(Context context);

    @NonNull
    public abstract Result next(Context context);

    public record Context(EncounterType type, ServerLevel level) {}

    public enum Result {
        SUCCESS, FAILURE, EMPTY;

        public boolean isSuccess() { return this == SUCCESS; }
    }
}
