package com.pz.beyond.api.system.progress;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * 关卡的状态，
 */
public enum ProgressState {
    /**
     * 玩家在安全区，关卡关闭
     */
    SAFE(false),

    /**
     * 玩家在安全区外面，但是并没有开启游戏
     */
    WAITING(true),

    /**
     * 正式开启游戏，并且玩家在外面空地
     */
    IN_PROGRESS_GROUND(true),

    /**
     * 玩家在节点内部。
     */
    IN_PROGRESS_NODE(true),

    /**
     * 位置状态
     */
    EMPTY(false);

    public static final Codec<ProgressState> CODEC = Codec.STRING.xmap(ProgressState::valueOf, ProgressState::name);

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgressState> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ProgressState decode(RegistryFriendlyByteBuf buf) {
            int stateId = buf.readVarInt();
            ProgressState[] states = ProgressState.values();
            if (stateId < 0 || stateId >= states.length) {
                return EMPTY;
            }
            return states[stateId];
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ProgressState state) {
            ProgressState safeState = state == null ? EMPTY : state;
            buf.writeVarInt(safeState.ordinal());
        }
    };

    ProgressState(final boolean inProgress){
        this.inProgress = inProgress;
    }
    private final boolean inProgress;

    public boolean isInProgress(){
        return this.inProgress;
    }
}
