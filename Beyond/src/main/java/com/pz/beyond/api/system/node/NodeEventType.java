package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.init.BeyondNodeEventTypes;
import com.pz.beyond.api.system.node.core.INodeEventType;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 事件激活类型，
 */
@Data
public abstract class NodeEventType implements INodeEventType {

    public static final Codec<NodeEventType> CODEC = ResourceLocation.CODEC.xmap(
        NodeEventType::fromId,
        NodeEventType::getIdentifier
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeEventType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NodeEventType decode(RegistryFriendlyByteBuf buf) {
            return fromId(buf.readResourceLocation());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, NodeEventType nodeEventType) {
            NodeEventType safe = nodeEventType == null ? BeyondNodeEventTypes.EMPTY : nodeEventType;
            buf.writeResourceLocation(safe.getIdentifier());
        }
    };


    private ResourceLocation identifier;

    public NodeEventType(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    private static NodeEventType fromId(ResourceLocation id) {
        NodeEventType type = BeyondNodeEventTypes.getById(id);
        return type == null ? BeyondNodeEventTypes.EMPTY : type;
    }



}
