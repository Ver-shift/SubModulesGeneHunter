package com.pz.beyond.api.event.handle;


import com.pz.beyond.api.init.BeyondAttachInit;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class ZoneEventHandle {


    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent event){

        var level = event.getLevel();
        if (level.isClientSide()) return;

        var safe_zone = level.getData(BeyondAttachInit.SAFE_ZONE);

    }
}
