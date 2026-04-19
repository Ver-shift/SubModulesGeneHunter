package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum NodeState implements StringRepresentable {
    /**
     * 锁定中，未进入
     */
    LOCKED("locked"),
    /**
     * 玩家进入节点，已 roll 出事件，等待触发
     */
    READY("ready"),
    /**
     * 正在执行事件列表中
     */
    ON_EVENT("on_event"),
    /**
     * 事件全部完成，节点显示为蓝色
     */
    COMPLETED("completed");

    private final String name;

    NodeState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<NodeState> CODEC = StringRepresentable.fromEnum(NodeState::values);
    public static final StreamCodec<ByteBuf, NodeState> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
