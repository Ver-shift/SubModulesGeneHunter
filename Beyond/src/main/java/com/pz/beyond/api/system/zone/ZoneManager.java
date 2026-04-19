package com.pz.beyond.api.system.zone;

import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.BeyondPlayerData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

public class ZoneManager {


    public void handleZoneRule(ServerLevel serverLevel){
        LevelZoneData levelZoneData = BeyondAPI.getBeyondLevelData(serverLevel).getLevelZoneData();
        Set<ZoneType> tickedZones = new HashSet<>();

        for (ServerPlayer player : serverLevel.players()) {
            BeyondPlayerData playerData = BeyondAPI.getBeyondPlayerData(player);

            ZoneType oldZone = playerData.getPlayerZoneData().getCurrentZone();
            ZoneData oldZoneData = levelZoneData.getZoneData(oldZone);
            ZoneData newZoneData = levelZoneData.getZoneData(player.getOnPos());
            ZoneType newZone = newZoneData.getZone();

            if (tickedZones.add(newZone)) {
                dispatchLevelTick(newZoneData, serverLevel, newZone);
            }

            dispatchPlayerTick(newZoneData, player, newZone);

            if (oldZone != newZone) {
                playerData.getPlayerZoneData().setCurrentZone(newZone);

                if (oldZone != BeyondZoneInit.EMPTY) {
                    dispatchZoneChange(oldZoneData, player, oldZone, newZone);
                    dispatchZoneChange(newZoneData, player, oldZone, newZone);
                }
            }
        }
    }
    private static void dispatchZoneChange(ZoneData zoneData, ServerPlayer player, ZoneType from, ZoneType to) {
        if (zoneData == null || zoneData.getListeners() == null) {
            return;
        }

        zoneData.getListeners().forEach(listener -> listener.getRule().playerChangeZone(player, from, to));
    }

    private static void dispatchPlayerTick(ZoneData zoneData, ServerPlayer player, ZoneType zoneType) {
        if (zoneData == null || zoneData.getListeners() == null || zoneType == null) {
            return;
        }

        zoneData.getListeners().forEach(listener -> listener.getRule().playerTick(player, zoneType));

    }

    private static void dispatchLevelTick(ZoneData zoneData, ServerLevel level, ZoneType zoneType) {
        if (zoneData == null || zoneData.getListeners() == null || zoneType == null) {
            return;
        }

        zoneData.getListeners().forEach(listener -> listener.getRule().levelTick(level, zoneType));
    }
}
