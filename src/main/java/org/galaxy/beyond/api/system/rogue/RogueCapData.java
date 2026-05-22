package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.init.BeyondRogueCapInit;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;

import java.util.Objects;

@Getter
@Setter
public class RogueCapData implements IPersistedSerializable {

    @Persisted
    private Identifier capId;
    @Persisted
    private int level;

    private transient RogueCap cap;

    public static final MapCodec<RogueCapData> CODEC = PersistedParser.createMapCodec(RogueCapData::new);
    public static final StreamCodec<ByteBuf, RogueCapData> STREAM_CODEC = PersistedParser.createStreamCodec(RogueCapData::new);

    public RogueCapData() {}

    public RogueCapData(RogueCap cap) {
        this.cap = cap;
        this.capId = cap.getId();
        this.level = 1;
    }

    public RogueCap getCap() {
        if (cap == null && capId != null) {
            cap = BeyondRogueCapInit.getById(capId)
                    .map(ref -> ref.value())
                    .orElse(RogueCap.EMPTY);
        }
        return cap != null ? cap : RogueCap.EMPTY;
    }

    public Identifier getIdentifier() {
        return getCap().getId();
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void addLevel(int delta) {
        setLevel(this.level + delta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RogueCapData that)) return false;
        return Objects.equals(getIdentifier(), that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier());
    }
}
