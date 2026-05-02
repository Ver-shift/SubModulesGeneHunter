package com.pz.beyond.api.system.node;

import com.pz.beyond.api.system.node.core.INodeEventManger;
import net.minecraft.server.level.ServerLevel;

public class NodeManager implements INodeEventManger {
    @Override
    public void init(NodeData nodeData) {
        //1.确立遭遇类型
        //2.抽取节点事件.
        //3.初始化节点事件数据，通过发布neoforge事件，进行动态修改节点事件类型。
        //4.
    }

    @Override
    public void tick(ServerLevel level) {
        //采用响应式布局
        var nodeState = NodeState.LOCKED;

        switch (nodeState) {
            case LOCKED:
            case READY:
            case ON_EVENT:
            case COMPLETED:
        }
    }

    @Override
    public void tryStart(ServerLevel level) {
        //玩家右键触发
        //1.检测关卡状态，是否锁定
        //2.检测当前服务器所有玩家，是不是都在这个节点区域里面
        //3.如果满足条件触发init方法。
    }

    @Override
    public void nextEvent() {

    }

    @Override
    public void tryEndNode() {

    }
}
