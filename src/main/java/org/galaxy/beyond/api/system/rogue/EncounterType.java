package org.galaxy.beyond.api.system.rogue;

import lombok.Getter;
import org.galaxy.beyond.api.system.node.NodeColor;

@Getter
public enum EncounterType {

    /**
     * 有趣的事件或者解密
     */
    Green_Event(NodeColor.GREEN, SceneType.HARVEST),
    /**
     * 篝火，纯粹的回复血量
     */
    Green_Bonfire(NodeColor.GREEN, SceneType.REPOSE),
    /**
     * 商店+Boss
     */
    Green_BossShop(NodeColor.GREEN, SceneType.CLIMAX),

    /**
     * 普通的怪物
     */
    Orange_NormalMonster(NodeColor.ORANGE, SceneType.HARVEST),
    /**
     * 普通商店
     */
    Orange_NormalShop(NodeColor.ORANGE, SceneType.REPOSE),
    /**
     * 商店+Boss
     */
    Orange_BossShop(NodeColor.ORANGE, SceneType.CLIMAX),

    /**
     * 精英怪挑战
     */
    Red_EliteMonster(NodeColor.RED, SceneType.HARVEST),
    /**
     * 诅咒商店
     */
    Red_CursedShop(NodeColor.RED, SceneType.REPOSE),
    /**
     * 商店+Boss
     */
    Red_BossShop(NodeColor.RED, SceneType.CLIMAX);

    private final NodeColor color;
    private final SceneType sceneType;

    EncounterType(NodeColor color, SceneType sceneType) {
        this.color = color;
        this.sceneType = sceneType;
    }
}
