package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.network.codec.StreamCodec;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import io.netty.buffer.ByteBuf;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;

/**
 * Phase 基类 —— 通过 Identifier 序列化，反序列化时从 Registry 解析。
 */
public abstract class Phase implements IPersistedSerializable {

    private final Identifier id;

    public Phase(Identifier id) {
        this.id = id;
    }

    public final Identifier getId() {
        return id;
    }

    public void enter(ServerLevel level, IRogueContext ctx) {}

    public void tick(ServerLevel level, IRogueContext ctx) {}

    public void exit(ServerLevel level, IRogueContext ctx) {}

    private static Phase resolve(Identifier id) {
        Phase p = BeyondPhaseInit.getRoguePhase(id);
        if (p != null) return p;
        p = BeyondPhaseInit.getPlayerPhase(id);
        if (p != null) return p;
        return BeyondPhaseInit.getNodePhase(id);
    }

    public static final MapCodec<Phase> CODEC =
            Identifier.CODEC.xmap(Phase::resolve, Phase::getId).fieldOf("id");

    public static final StreamCodec<ByteBuf, Phase> STREAM_CODEC =
            Identifier.STREAM_CODEC.map(Phase::resolve, Phase::getId);
}
