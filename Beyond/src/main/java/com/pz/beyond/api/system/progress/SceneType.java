package com.pz.beyond.api.system.progress;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 进度事件类型
 */
public enum SceneType implements StringRepresentable {
    /**
     * 资源事件：用于获取资源的房间，可能是战斗或小游戏
     */
    HARVEST("beyond:harvest"),
    /**
     * 修正事件：用于调整对局节奏，恢复状态或强化自身
     */
    REPOSE("beyond:repose"),
    /**
     * BOSS挑战事件：必定出现的boss节点，包含商店
     */
    CLIMAX("beyond:climax"),

    /**
     * 空类型
     */
    EMPTY("beyond:empty");

    private final String name;

    SceneType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final Codec<SceneType> CODEC = StringRepresentable.fromEnum(SceneType::values);
    public static final StreamCodec<ByteBuf, SceneType> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
