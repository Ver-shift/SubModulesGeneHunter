package com.pz.beyond.nodeTask;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.NodeEventType;
import net.minecraft.resources.ResourceLocation;

//治疗玩家
public class HealEvent extends NodeEventType {
    public static final ResourceLocation HEAL_EVENT = Beyond.asResource("heal_event");

    public HealEvent() {
        super(HEAL_EVENT);
    }

    @Override
    public void cast(Context context) {

    }

    @Override
    public Result canNextEvent(Context context) {
        return Result.defaulted();
    }
}
