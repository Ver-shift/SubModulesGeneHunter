package org.galaxy.beyond.zone_cap;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.zone.CapType;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.Arrays;

public class AllSafeZoneCap extends ZoneCapType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":all_safe_zone");

    public AllSafeZoneCap() {
        super(ID);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public CapType getCapType() {
        return CapType.NORMAL;
    }

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        Beyond.debugInfo("player change from" + Arrays.toString(new String[]{from.getName()}) + " to Zone" + Arrays.toString(new String[]{to.getName()}));
    }
}
