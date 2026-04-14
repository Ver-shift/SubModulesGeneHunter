package com.pz.beyond.api.system.progress;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 进度事件类型
 */
public enum SceneType {
    /**
     * 资源事件：用于获取资源的房间，可能是战斗或小游戏
     */
    HARVEST,
    /**
     * 修正事件：用于调整对局节奏，恢复状态或强化自身
     */
    REPOSE,
    /**
     * BOSS挑战事件：必定出现的boss节点，包含商店
     */
    CLIMAX;

    public static final Codec<SceneType> CODEC = Codec.STRING.xmap(SceneType::valueOf, SceneType::name);

    public static final StreamCodec<RegistryFriendlyByteBuf, SceneType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SceneType decode(RegistryFriendlyByteBuf buf) {
            int typeId = buf.readVarInt();
            SceneType[] types = SceneType.values();
            if (typeId < 0 || typeId >= types.length) {
                return HARVEST;
            }
            return types[typeId];
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SceneType sceneType) {
            SceneType safeType = sceneType == null ? HARVEST : sceneType;
            buf.writeVarInt(safeType.ordinal());
        }
    };
}
