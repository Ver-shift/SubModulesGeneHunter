package org.galaxy.beyond.api.system.zone.core;

import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.List;

public interface IZoneManager {

    /**
     * 安全区控制，通过寻找结构进行初始化
     */
    void safeZone();

    /**
     * 每次结构区域加载的时候进行创建
     */
    void nodeZone();

    /**
     * 玩家可活动区域,通过玩家节点活动进行自动扩容。记得想办法保持玩家的最小加载区域
     */
    void activeZone();

    //增删查改区域的cap
    void addCap(ZoneType type, ZoneCapType zoneCapType);

    void removeCap(ZoneType type, ZoneCapType zoneCapType);

    void clearCap(ZoneType type);

    List<ZoneCapType> getCaps(ZoneType type);

}
