package com.pz.beyond.api;

import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.zone.LevelZoneData;
import com.pz.beyond.api.system.zone.SaveLevelData;
import com.pz.beyond.api.system.zone.ZoneData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;

public class BeyondAPI {

    @OnlyIn(Dist.DEDICATED_SERVER)
    public static LevelZoneData getLevelZoneData(ResourceKey<Level> level) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return new LevelZoneData();
        }
        return SaveLevelData.get(server).getOrCreateLevelData(Level.OVERWORLD);
    }

    public static LevelZoneData getOverworldZoneData(){
        return getLevelZoneData(Level.OVERWORLD);
    }
}
