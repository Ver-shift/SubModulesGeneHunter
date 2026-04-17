package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.progress.SceneType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 节点事件类型，由节点和颜色决定的类型。
 */
public class EncounterType {

    public static final String ID = "id";

    @Getter
    private NodeColor color;
    @Getter
    private SceneType sceneType;
    @Getter
    private ResourceLocation identifier;

    protected EncounterType(ResourceLocation identifier,SceneType sceneType,NodeColor color) {
        this.color = color;
        this.sceneType = sceneType;
        this.identifier = identifier;
    }


    private EncounterType(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public static final Codec<EncounterType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf(ID).forGetter(EncounterType::getIdentifier)

    ).apply(builder, EncounterType::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncounterType> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            EncounterType::getIdentifier,
            EncounterType::new
    );



}
