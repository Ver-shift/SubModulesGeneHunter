package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 节点颜色
 */
public enum NodeColor {
    /**
     * 绿色：安全，基本上没有风险
     */
    GREEN(0x00FF00),
    /**
     * 蓝色：已解锁过的节点（完成后的渲染状态）
     */
    BLUE(0x0088FF),
    /**
     * 橙色：机遇，未解锁的挑战等待解锁
     */
    ORANGE(0xFF8800),
    /**
     * 红色：危险，有更多的机遇和更多的挑战
     */
    RED(0xFF0000);

    public static final Codec<NodeColor> CODEC = Codec.STRING.xmap(NodeColor::valueOf, NodeColor::name);

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeColor> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NodeColor decode(RegistryFriendlyByteBuf buf) {
            int colorId = buf.readVarInt();
            NodeColor[] colors = NodeColor.values();
            if (colorId < 0 || colorId >= colors.length) {
                return GREEN;
            }
            return colors[colorId];
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, NodeColor nodeColor) {
            NodeColor safeColor = nodeColor == null ? GREEN : nodeColor;
            buf.writeVarInt(safeColor.ordinal());
        }
    };

    private final int color;

    NodeColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
