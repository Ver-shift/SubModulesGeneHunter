package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.init.BeyondPhaseInit;

/**
 * 事件类型 —— 通过 Identifier 序列化，反序列化时从 Registry 解析。
 */
public abstract class RogueEventType implements IPersistedSerializable {
    private final Identifier id;

    public RogueEventType(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    public Component getDisplayName() {
        return Component.translatable(id.getNamespace() + ".event." + id.getPath());
    }

    public abstract void cast(Context context);

    @NonNull
    public abstract Result next(Context context);

    public record Context(EncounterType type, ServerLevel level) {}

    public enum Result {
        SUCCESS, FAILURE, EMPTY;

        public boolean isSuccess() { return this == SUCCESS; }
    }

    // ============================================================
    // 持久化 —— Identifier xmap，从 Registry 解析
    // ============================================================

    private static RogueEventType resolve(Identifier id) {
        return BeyondPhaseInit.getRogueEventType(id);
    }

    public static final MapCodec<RogueEventType> CODEC =
            Identifier.CODEC.xmap(RogueEventType::resolve, RogueEventType::getId).fieldOf("id");

    public static final StreamCodec<ByteBuf, RogueEventType> STREAM_CODEC =
            Identifier.STREAM_CODEC.map(RogueEventType::resolve, RogueEventType::getId);
}
