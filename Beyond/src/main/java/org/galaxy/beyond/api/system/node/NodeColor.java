package org.galaxy.beyond.api.system.node;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum NodeColor implements IPersistedSerializable {
    BLUE(0x0000FF),
    GREEN(0x00FF00),
    ORANGE(0xFFA500),
    RED(0xFF0000),
    EMPTY(0xFFFFFF);

    @Persisted
    private final int colorValue;

    NodeColor(int colorValue) {
        this.colorValue = colorValue;
    }

    private static final Codec<NodeColor> C = Codec.STRING.xmap(
            name -> { try { return valueOf(name); } catch (IllegalArgumentException e) { return EMPTY; } },
            Enum::name);
    public static final MapCodec<NodeColor> CODEC = C.fieldOf("name");
    public static final StreamCodec<ByteBuf, NodeColor> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .map(name -> { try { return valueOf(name); } catch (IllegalArgumentException e) { return EMPTY; } },
                 Enum::name);

    public int getColorValue() {
        return colorValue;
    }

    @SuppressWarnings("unused")
    private static NodeColor valueOfPersisted(int colorValue) {
        for (var v : values())
            if (v.colorValue == colorValue) return v;
        return EMPTY;
    }
}
