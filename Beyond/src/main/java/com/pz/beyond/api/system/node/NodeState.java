package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum NodeState {
    /**
     * 锁定中，未进入
     */
    LOCKED,
    /**
     * 玩家进入节点，已 roll 出事件，等待触发
     */
    READY,
    /**
     * 正在执行事件列表中
     */
    ON_EVENT,
    /**
     * 事件全部完成，节点显示为蓝色
     */
    COMPLETED;

    public static final Codec<NodeState> CODEC = Codec.STRING.xmap(NodeState::valueOf, NodeState::name);

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeState> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NodeState decode(RegistryFriendlyByteBuf buf) {
            int stateId = buf.readVarInt();
            NodeState[] states = NodeState.values();
            if (stateId < 0 || stateId >= states.length) {
                return LOCKED;
            }
            return states[stateId];
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, NodeState nodeState) {
            NodeState safeState = nodeState == null ? LOCKED : nodeState;
            buf.writeVarInt(safeState.ordinal());
        }
    };
}
