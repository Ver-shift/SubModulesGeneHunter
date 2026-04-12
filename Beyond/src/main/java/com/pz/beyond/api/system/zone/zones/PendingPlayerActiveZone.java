package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.resources.ResourceLocation;

/**
 * 玩家待活动区域，用于玩家计策
 */
public class PendingPlayerActiveZone extends AbstractZone {

    public static final ResourceLocation PENDING_PLAYER_ACTIVE_ZONE = Beyond.asResource("pending_player_active_zone");

    public PendingPlayerActiveZone() {
        super(PENDING_PLAYER_ACTIVE_ZONE);
    }
}
