package com.pz.beyond.api.system.progress.core;

import com.pz.beyond.api.system.node.RolledData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public interface IProgressManager {

    //设置并且初始化当前的关卡节点。
    void initProgress(ResourceLocation progressID);
    //初始化一个节点

    void initNode(ServerLevel level,Long chunkKey);

    //右键点击节点方块，自动判定当前的区域还有节点状态，还有右键事件步进。
    void rightClickCenter(ServerPlayer player);

    //根据事件列表依次触发方法
    void eventHandle(RolledData data);

    /**
     * chunk的结构加载的时候，同时进行节点区域的颜色设立，注意一个结构只能有一个节点
     * @param level
     */
    void chunkLoad(ServerLevel level);
}
