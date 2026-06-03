package org.galaxy.beyond.api.system.node;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum NodeColor implements IPersistedSerializable {
    /** 已解锁或已完成的节点。 */
    BLUE(0x0000FF),
    /** 绿色未完成节点，通常对应较轻量的事件。 */
    GREEN(0x00FF00),
    /** 橙色未完成节点，通常对应普通遭遇。 */
    ORANGE(0xFFA500),
    /** 红色未完成节点，通常对应高风险遭遇。 */
    RED(0xFF0000),
    /** 空节点或无法解析时的兜底值。 */
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
