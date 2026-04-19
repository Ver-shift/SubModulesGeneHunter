package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 节点颜色
 */
public enum NodeColor implements StringRepresentable {
    /**
     * 空颜色：用于兜底，避免 null。
     */
    EMPTY("empty", 0x000000),

    /**
     * 绿色：安全，基本上没有风险
     */
    GREEN("green", 0x00FF00),
    /**
     * 蓝色：已解锁过的节点（完成后的渲染状态）
     */
    BLUE("blue", 0x0088FF),
    /**
     * 橙色：机遇，未解锁的挑战等待解锁
     */
    ORANGE("orange", 0xFF8800),
    /**
     * 红色：危险，有更多的机遇和更多的挑战
     */
    RED("red", 0xFF0000);

    private final String name;
    private final int color;

    NodeColor(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<NodeColor> CODEC = StringRepresentable.fromEnum(NodeColor::values);
    public static final StreamCodec<ByteBuf, NodeColor> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
