package com.pz.beyond.safeZoneRule.impl;

import com.pz.beyond.data.SafeZoneData;
import com.pz.beyond.registry.ModTags;
import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import com.pz.beyond.safeZoneRule.SafeZoneRule;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@SafeZoneRule
public class NoMobSpawn implements ISafeZoneRuleListener {

    @Override
    public void onMobSpawn(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            SafeZoneData safeZone = serverLevel.getServer().getLevel(Level.OVERWORLD).getDataStorage()
                    .computeIfAbsent(new SavedData.Factory<>(SafeZoneData::create, SafeZoneData::load), "SafeZone");
            if (event.getEntity().getType().is(ModTags.NO_SAFE_ZONE_SPAWN)) {
                if (safeZone.isWithinSafeZone(event.getX(),event.getZ())) {
                    event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
                }
            }
        }
    }

}
