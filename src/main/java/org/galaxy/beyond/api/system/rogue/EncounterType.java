package org.galaxy.beyond.api.system.rogue;

import lombok.Getter;
import org.galaxy.beyond.api.system.node.NodeColor;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * 遭遇类型 = SceneType(进度类型) × NodeColor(节点颜色) 的 9 种组合。
 * 蓝色节点表示已解锁，无遭遇。
 */
@Getter
public enum EncounterType {

    Green_Event(NodeColor.GREEN, SceneType.HARVEST),
    Green_Bonfire(NodeColor.GREEN, SceneType.REPOSE),
    Green_BossShop(NodeColor.GREEN, SceneType.CLIMAX),

    Orange_NormalMonster(NodeColor.ORANGE, SceneType.HARVEST),
    Orange_NormalShop(NodeColor.ORANGE, SceneType.REPOSE),
    Orange_BossShop(NodeColor.ORANGE, SceneType.CLIMAX),

    Red_EliteMonster(NodeColor.RED, SceneType.HARVEST),
    Red_CursedShop(NodeColor.RED, SceneType.REPOSE),
    Red_BossShop(NodeColor.RED, SceneType.CLIMAX);

    private final NodeColor color;
    private final SceneType sceneType;

    EncounterType(NodeColor color, SceneType sceneType) {
        this.color = color;
        this.sceneType = sceneType;
    }

    private static final EnumMap<NodeColor, EnumMap<SceneType, EncounterType>> LOOKUP = new EnumMap<>(NodeColor.class);
    static {
        for (var et : values()) {
            LOOKUP.computeIfAbsent(et.color, c -> new EnumMap<>(SceneType.class)).put(et.sceneType, et);
        }
    }

    /** 根据节点颜色和进度类型查找对应的遭遇类型，蓝色节点返回空 */
    public static EncounterType from(NodeColor color, SceneType scene) {
        var byScene = LOOKUP.get(color);
        return byScene != null ? byScene.get(scene) : null;
    }

    /** 返回指定 SceneType 下所有可能的 EncounterType (排除 BLUE) */
    public static EnumSet<EncounterType> byScene(SceneType scene) {
        var result = EnumSet.noneOf(EncounterType.class);
        for (var et : values()) {
            if (et.sceneType == scene) result.add(et);
        }
        return result;
    }
}
