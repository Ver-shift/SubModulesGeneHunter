package com.pz.beyond.api.system.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 一局游戏的运行时数据
 */
@Data
public class Progress {

    public static final String TYPE_ID = "type_id";
    public static final String SEED = "seed";
    public static final String SCENES = "scenes";
    public static final String CURRENT_SCENE_INDEX = "current_scene_index";

    public static final Codec<Progress> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf(TYPE_ID).forGetter(Progress::getTypeId),
        Codec.LONG.optionalFieldOf(SEED, 0L).forGetter(Progress::getSeed),
        Scene.CODEC.listOf().optionalFieldOf(SCENES, List.of()).forGetter(Progress::getScenes),
        Codec.INT.optionalFieldOf(CURRENT_SCENE_INDEX, 0).forGetter(Progress::getCurrentSceneIndex)
    ).apply(builder, Progress::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Long> SEED_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Long decode(RegistryFriendlyByteBuf buf) {
            return buf.readLong();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Long seed) {
            buf.writeLong(seed == null ? 0L : seed);
        }
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Scene>> SCENE_LIST_STREAM_CODEC =
        ByteBufCodecs.collection(ArrayList::new, Scene.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, Progress> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        Progress::getTypeId,
        SEED_STREAM_CODEC,
        Progress::getSeed,
        SCENE_LIST_STREAM_CODEC,
        Progress::getScenes,
        ByteBufCodecs.VAR_INT,
        Progress::getCurrentSceneIndex,
        Progress::new
    );

    private static final ResourceLocation EMPTY_TYPE_ID = ResourceLocation.withDefaultNamespace("empty");

    /**
     * 引用的模板（数据库层）
     */
    private ProgressType type;
    private ResourceLocation typeId = EMPTY_TYPE_ID;

    /**
     * 随机种子
     */
    private long seed; // TODO: 暂时自己生成

    /**
     * 随机源，用于实时 roll 事件
     */
    private Random random;

    /**
     * 进度条，多个 Scene 组成
     */
    private List<Scene> scenes = new ArrayList<>();

    /**
     * 当前进度索引
     */
    private int currentSceneIndex = 0;

    public Progress() {
    }

    /**
     * 从模板创建一局游戏
     */
    public static Progress create(ProgressType type) {
        Progress progress = new Progress();
        progress.type = type;
        progress.typeId = type == null ? EMPTY_TYPE_ID : type.getId();
        progress.seed = new Random().nextLong(); // TODO: 外部传入随机源
        progress.random = new Random(progress.seed);

        // 根据模板生成进度条
        for (ProgressType.SceneEntry entry : type.getScenes()) {
            progress.scenes.add(new Scene(entry.sceneType()));
        }
        return progress;
    }

    public Progress(ResourceLocation typeId, long seed, List<Scene> scenes, int currentSceneIndex) {
        this.typeId = typeId == null ? EMPTY_TYPE_ID : typeId;
        this.seed = seed;
        this.random = new Random(seed);
        this.scenes = scenes == null ? new ArrayList<>() : new ArrayList<>(scenes);
        this.currentSceneIndex = Math.max(0, currentSceneIndex);
    }

    public ResourceLocation getTypeId() {
        if (type != null) {
            return type.getId();
        }
        return typeId == null ? EMPTY_TYPE_ID : typeId;
    }

    /**
     * 获取当前 Scene
     */
    public Scene getCurrentScene() {
        if (currentSceneIndex >= scenes.size()) {
            return null;
        }
        return scenes.get(currentSceneIndex);
    }

    /**
     * 完成当前节点，检查是否需要步进 Scene
     */
    public void advanceScene() {
        Scene current = getCurrentScene();
        if (current != null) {
            current.setCompleted(true);
            currentSceneIndex++;
        }
    }

    /**
     * 是否全部完成
     */
    public boolean isFinished() {
        return currentSceneIndex >= scenes.size();
    }

    public int getNodesCount() {
        return scenes.size();
    }

    /**
     * 初始化游戏，预生成数据
     */
    public void initialize() {
        // TODO: 实现游戏初始化逻辑，预生成节点事件等
        this.currentSceneIndex = 0;
        for (Scene scene : scenes) {
            scene.setCompleted(false);
        }
    }
}
