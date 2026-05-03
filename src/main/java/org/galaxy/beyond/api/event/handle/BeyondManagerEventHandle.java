package org.galaxy.beyond.api.event.handle;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.galaxy.beyond.api.system.BeyondAPI;

@EventBusSubscriber
public class BeyondManagerEventHandle {

    @SubscribeEvent
    public void levelTick(LevelTickEvent.Pre event) {

        if (event.getLevel() instanceof ServerLevel level) {
            BeyondAPI.getBeyondManager().levelTick(level);
        }
    }

    @SubscribeEvent
    public void livingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BeyondAPI.getBeyondManager().playerTick(player);
        }
        if (event.getEntity() instanceof LivingEntity entity) {
            BeyondAPI.getBeyondManager().entityTick(entity);
        }
    }

    @SubscribeEvent
    public void playerLoginIn(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getEntity() instanceof ServerPlayer player){
            BeyondAPI.getBeyondManager().playerLogin(player);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event){
        LevelChunk chunk = event.getChunk();
        if (chunk.getLevel() instanceof ServerLevel level) {
            BeyondAPI.getBeyondManager().onChunkLoad(chunk);
        }
    }


}
