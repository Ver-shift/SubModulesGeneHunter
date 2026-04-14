package com.pz.beyond.api.system.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 进度条上的单个节点，纯进度标记
 */
@Data
public class Scene {

    public static final String SCENE_TYPE = "scene_type";
    public static final String COMPLETED = "completed";

    public static final Codec<Scene> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        SceneType.CODEC.fieldOf(SCENE_TYPE).forGetter(Scene::getSceneType),
        Codec.BOOL.optionalFieldOf(COMPLETED, false).forGetter(Scene::isCompleted)
    ).apply(builder, Scene::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Scene> STREAM_CODEC = StreamCodec.composite(
        SceneType.STREAM_CODEC,
        Scene::getSceneType,
        ByteBufCodecs.BOOL,
        Scene::isCompleted,
        Scene::new
    );

    private final SceneType sceneType;
    private boolean completed = false;

    public Scene(SceneType sceneType) {
        this.sceneType = sceneType;
    }

    public Scene(SceneType sceneType, boolean completed) {
        this.sceneType = sceneType;
        this.completed = completed;
    }

}
