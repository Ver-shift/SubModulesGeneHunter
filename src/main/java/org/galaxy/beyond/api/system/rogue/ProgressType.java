package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProgressType implements IPersistedSerializable {

    @Persisted
    private Identifier id;

    @Persisted
    private List<SceneType> scenes = new ArrayList<>();
    @Persisted
    private int scenesIndex;
    @Persisted
    private List<EncounterData> encounters = new ArrayList<>();

    public static final MapCodec<ProgressType> CODEC = PersistedParser.createMapCodec(ProgressType::new);
    public static final StreamCodec<ByteBuf, ProgressType> STREAM_CODEC = PersistedParser.createStreamCodec(ProgressType::new);

    public ProgressType() {}

    public ProgressType(Identifier id) {
        this.id = id;
    }
}
