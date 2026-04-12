package com.pz.beyond.api.system.progress;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * 进度类型定义，包含多个 Scene 节点
 */
public class ProgressType {

    private final ResourceLocation id;
    private final List<SceneEntry> scenes;

    private ProgressType(ResourceLocation id, List<SceneEntry> scenes) {
        this.id = id;
        this.scenes = scenes;
    }



    /**
     * 创建新的 ProgressType
     */
    public static ProgressType create(ResourceLocation id, Consumer<Builder> consumer) {
        Builder builder = new Builder(id);
        consumer.accept(builder);
        return builder.build();
    }

    public ResourceLocation getId() {
        return id;
    }

    public List<SceneEntry> getScenes() {
        return scenes;
    }

    /**
     * 场景节点
     */
    public record SceneEntry(int displayWeight, SceneType sceneType) {
    }

    /**
     * 构建器
     */
    public static class Builder {
        private final ResourceLocation id;
        private final List<SceneEntry> scenes = new ArrayList<>();

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        /**
         * 添加场景节点
         *
         * @param displayWeight 显示权重（越小越排在前面）
         * @param sceneType     场景类型
         */
        public Builder scene(int displayWeight, SceneType sceneType) {
            scenes.add(new SceneEntry(displayWeight, sceneType));
            return this;
        }

        /**
         * 构建 ProgressType
         */
        public ProgressType build() {
            scenes.sort(Comparator.comparingInt(SceneEntry::displayWeight));
            return new ProgressType(id, List.copyOf(scenes));
        }
    }
}
