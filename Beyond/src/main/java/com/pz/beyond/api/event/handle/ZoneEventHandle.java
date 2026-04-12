package com.pz.beyond.api.event.handle;


import com.pz.beyond.api.init.BeyondAttachInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class ZoneEventHandle {


    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent event){

        var level = event.getLevel();
        if (level.isClientSide()) return;


    }
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event ){
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        var dimensionKey = level.dimension();



    }
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event){
        if (!(event.getLevel() instanceof ServerLevel level)) return;




    }
}
