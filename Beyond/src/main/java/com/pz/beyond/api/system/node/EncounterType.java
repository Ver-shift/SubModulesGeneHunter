package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.pz.beyond.api.init.BeyondEncounters;
import com.pz.beyond.api.system.progress.SceneType;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 节点事件类型，由节点和颜色决定的类型。
 */
public class EncounterType {

    @Getter
    private NodeColor color = NodeColor.EMPTY;
    @Getter
    private SceneType sceneType = SceneType.EMPTY;
    @Getter
    private ResourceLocation identifier;

    public EncounterType(ResourceLocation identifier, SceneType sceneType, NodeColor color) {
        this.identifier = identifier;
        this.sceneType = sceneType == null ? SceneType.EMPTY : sceneType;
        this.color = color == null ? NodeColor.EMPTY : color;
    }


    private EncounterType(ResourceLocation identifier) {
        EncounterType resolved = fromId(identifier);
        this.identifier = resolved.getIdentifier();
        this.sceneType = resolved.getSceneType();
        this.color = resolved.getColor();
    }

    private static EncounterType fromId(ResourceLocation id) {
        return BeyondEncounters.getById(id);
    }

    public static final Codec<EncounterType> CODEC = ResourceLocation.CODEC.xmap(
            EncounterType::fromId,
            BeyondEncounters::getId
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EncounterType> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EncounterType decode(RegistryFriendlyByteBuf buf) {
            return fromId(buf.readResourceLocation());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, EncounterType encounterType) {
            EncounterType safe = encounterType == null ? BeyondEncounters.EMPTY : encounterType;
            buf.writeResourceLocation(BeyondEncounters.getId(safe));
        }
    };



}
