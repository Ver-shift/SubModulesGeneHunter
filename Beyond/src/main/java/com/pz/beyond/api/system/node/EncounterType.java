package com.pz.beyond.api.system.node;

import com.pz.beyond.api.system.progress.SceneType;

import java.util.function.Supplier;

/**
 * 节点事件类型，由节点和颜色决定的类型。
 */
public class EncounterType implements IEncounterType {

    private NodeColor color;
    private SceneType sceneType;

    public EncounterType(Supplier<NodeColor> color, Supplier<SceneType> sceneType) {
        this(color.get(), sceneType.get());
    }

    public EncounterType(NodeColor color, SceneType sceneType) {
        this.color = color;
        this.sceneType = sceneType;
    }

    /**
     * 用于不需要颜色的遭遇类型
     */
    public EncounterType(Supplier<SceneType> sceneType) {
        this.sceneType = sceneType.get();
    }
}
