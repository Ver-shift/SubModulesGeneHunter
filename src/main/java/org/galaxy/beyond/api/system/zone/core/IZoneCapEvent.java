package org.galaxy.beyond.api.system.zone.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.zone.ZoneType;

public interface IZoneCapEvent {

    default void levelTick(ServerLevel level, Context context){

    }

    default void playerTick(ServerPlayer player,Context context){

    }

    /**
     *
     * @param player
     * @param from 从哪个区域来的
     * @param context 往哪个区域去的
     */
    default void changeZone(ServerPlayer player,ZoneType from,Context context){

    }

    public record Context(int capLevel,ZoneType zoneType) {

    }
}
