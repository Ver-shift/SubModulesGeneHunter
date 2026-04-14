package com.pz.beyond.api.system.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.progress.SceneType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * 节点事件类型，由节点和颜色决定的类型。
 */
public class EncounterType implements IEncounterType {

    public static final String COLOR = "color";
    public static final String SCENE_TYPE = "scene_type";

    public static final Codec<EncounterType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        NodeColor.CODEC.optionalFieldOf(COLOR).forGetter(EncounterType::getColorOptional),
        SceneType.CODEC.fieldOf(SCENE_TYPE).forGetter(EncounterType::getSceneType)
    ).apply(builder, EncounterType::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EncounterType> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.optional(NodeColor.STREAM_CODEC),
        EncounterType::getColorOptional,
        SceneType.STREAM_CODEC,
        EncounterType::getSceneType,
        EncounterType::new
    );

    private NodeColor color;
    private SceneType sceneType;

    public EncounterType(NodeColor color, SceneType sceneType) {
        this.color = color;
        this.sceneType = sceneType;
    }

    public EncounterType(Optional<NodeColor> color, SceneType sceneType) {
        this(color.orElse(null), sceneType);
    }

    /**
     * 用于不需要颜色的遭遇类型
     */
    public EncounterType(SceneType sceneType) {
        this.sceneType = sceneType;
    }

    public NodeColor getColor() {
        return color;
    }

    public SceneType getSceneType() {
        return sceneType;
    }

    private Optional<NodeColor> getColorOptional() {
        return Optional.ofNullable(color);
    }
}
