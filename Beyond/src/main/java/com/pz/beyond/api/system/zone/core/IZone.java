package com.pz.beyond.api.system.zone.core;

import com.pz.beyond.api.system.rule.IZoneRule;
import net.minecraft.resources.ResourceLocation;

public interface IZone<T extends IZoneRule> {

    /**
     * 唯一标识符
     * @return 区域标识符
     */
    ResourceLocation getIdentifier();

    /**
     * 添加事件监听器
     * @param listener 监听器
     */
    void addListener(T listener);

    /**
     * 移除事件监听器
     * @param listener 监听器
     */
    void removeListener(T listener);

    /**
     * 获取所有监听器（高性能，线程安全）
     * @return 监听器数组（快照，遍历时不会被修改）
     */
    IZoneRule[] getListeners();
}
