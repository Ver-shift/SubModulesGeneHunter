package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import org.galaxy.beyond.api.system.node.NodeColor;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * 遭遇类型 = SceneType(进度类型) x NodeColor(节点颜色) 的 9 种组合。
 */
@Getter
public enum EncounterType implements IPersistedSerializable {

    Green_Event(NodeColor.GREEN, SceneType.HARVEST),
    Green_Bonfire(NodeColor.GREEN, SceneType.REPOSE),
    Green_BossShop(NodeColor.GREEN, SceneType.CLIMAX),

    Orange_NormalMonster(NodeColor.ORANGE, SceneType.HARVEST),
    Orange_NormalShop(NodeColor.ORANGE, SceneType.REPOSE),
    Orange_BossShop(NodeColor.ORANGE, SceneType.CLIMAX),

    Red_EliteMonster(NodeColor.RED, SceneType.HARVEST),
    Red_CursedShop(NodeColor.RED, SceneType.REPOSE),
    Red_BossShop(NodeColor.RED, SceneType.CLIMAX);

    @Persisted
    private final String id;
    private final NodeColor color;
    private final SceneType sceneType;

    EncounterType(NodeColor color, SceneType sceneType) {
        this.id = name();
        this.color = color;
        this.sceneType = sceneType;
    }

    public String getTranslationKey() {
        return "beyond.encounter." + name().toLowerCase();
    }

    private static final EnumMap<NodeColor, EnumMap<SceneType, EncounterType>> LOOKUP = new EnumMap<>(NodeColor.class);
    static {
        for (var et : values()) {
            LOOKUP.computeIfAbsent(et.color, c -> new EnumMap<>(SceneType.class)).put(et.sceneType, et);
        }
    }

    public static EncounterType from(NodeColor color, SceneType scene) {
        var byScene = LOOKUP.get(color);
        return byScene != null ? byScene.get(scene) : null;
    }

    public static EnumSet<EncounterType> byScene(SceneType scene) {
        var result = EnumSet.noneOf(EncounterType.class);
        for (var et : values()) {
            if (et.sceneType == scene) result.add(et);
        }
        return result;
    }

    @SuppressWarnings("unused")
    private static EncounterType valueOfPersisted(String id) {
        try { return valueOf(id); } catch (IllegalArgumentException e) { return Green_Event; }
    }
}
