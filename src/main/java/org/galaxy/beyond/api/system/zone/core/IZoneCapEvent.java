package org.galaxy.beyond.api.system.zone.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.api.system.zone.ZoneType;

public interface IZoneCapEvent {

    void levelTick(ServerLevel level, ZoneType type);

    void playerTick(ServerPlayer player,ZoneType type);

    void changeZone(ServerPlayer player,ZoneType from,ZoneType to);
}
