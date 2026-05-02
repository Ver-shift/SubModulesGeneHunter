package com.pz.beyond.api.system.node.core;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

public interface INodeEventManger {

    /**
     * 初始化当前节点的事件.
     */
    void init(NodeData nodeData);

    /**
     * tick循环，负责检测一些数据
     * @param level
     */
    void tick(ServerLevel level);

    /**
     * 尝试启动一个节点，并且变化节点状态
     *
     *         //1.检测关卡状态，是否锁定
     *         //2.检测当前服务器所有玩家，是不是都在这个节点区域里面
     *         //3.如果满足条件触发init方法。
     */
    void tryStart(ServerLevel level);
    /**
     * 尝试切换到下一个事件。并且给玩家输出文本
     */
    void nextEvent();


    /**
     * 尝试永久解锁这个节点，以后永远都不需要交互了。
     */
    void tryEndNode();
}
