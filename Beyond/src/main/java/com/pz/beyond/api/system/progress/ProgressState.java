package com.pz.beyond.api.system.progress;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 关卡的状态，
 */
public enum ProgressState implements StringRepresentable {
    /**
     * 玩家在安全区，关卡关闭
     */
    SAFE("safe", false),

    /**
     * 玩家在安全区外面，但是并没有开启游戏
     */
    WAITING("waiting", true),

    /**
     * 正式开启游戏，并且玩家在外面空地
     */
    IN_PROGRESS_GROUND("in_progress_ground", true),

    /**
     * 玩家在节点内部。
     */
    IN_PROGRESS_NODE("in_progress_node", true),

    /**
     * 位置状态
     */
    EMPTY("empty", false);

    private final String name;
    private final boolean inProgress;

    ProgressState(String name, boolean inProgress) {
        this.name = name;
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return this.inProgress;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<ProgressState> CODEC = StringRepresentable.fromEnum(ProgressState::values);
    public static final StreamCodec<ByteBuf, ProgressState> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
