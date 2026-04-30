package com.pz.beyond.nodeTask;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.NodeEventType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BossEvent extends NodeEventType {
    public static final ResourceLocation BOSS_EVENT = Beyond.asResource("boss_event");

    public BossEvent() {
        super(BOSS_EVENT);
    }

    /**
     * 召唤怪物
     * @param context
     */
    @Override
    public void cast(Context context) {
        context.players().stream().forEach(player -> {player.sendSystemMessage(Component.literal("text_boss"));});

    }

    /**
     * 怪物都死完了
     * @param context
     * @return 
     */
    @Override
    public Result canNextEvent(Context context) {
        //TODO :刷怪系统完善了再做吧。




        return Result.success(Component.literal("text_bossEvent"));
    }
}
