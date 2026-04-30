package com.pz.beyond.nodeTask;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.NodeEventType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ShopEvent extends NodeEventType {

    public static final ResourceLocation SHOP = Beyond.asResource("shop");
    public ShopEvent() {
        super(SHOP);
    }

    @Override
    public void cast(Context context) {
        context.players().stream().forEach(player -> {player.sendSystemMessage(Component.literal("text_shop"));});

    }

    @Override
    public Result canNextEvent(Context context) {
        return Result.success(Component.literal("text_shop_Event"));
    }
}
