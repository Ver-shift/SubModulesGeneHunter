package com.pz.beyond.api.system.node.core;

import com.pz.beyond.api.system.node.NodeData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface INodeEventType {

    //触发方法，在节点方块位置为中心
    void cast(Context context);

    Result canNextEvent(Context context);




    record Context(List<ServerPlayer> players, ServerLevel level, NodeData nodeData){

    }

    record Result(boolean isSuccuss, Component Info){
        public static Result success(Component info){
            return new Result(true, info);
        }
        public static Result fail(Component info) {
            return new Result(false, info);
        }

        public static Result defaulted(){
            return new Result(false, Component.literal("unknown_question"));
        }
    }


}
