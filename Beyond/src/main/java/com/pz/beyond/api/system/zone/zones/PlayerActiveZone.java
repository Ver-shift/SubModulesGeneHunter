package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.resources.ResourceLocation;

/**
 * 玩家可活动区域
 */
public class PlayerActiveZone extends AbstractZone {

    public static final ResourceLocation PLAYER_ACTIVE_ZONE = Beyond.asResource("player_active_zone");

    public PlayerActiveZone() {
        super(PLAYER_ACTIVE_ZONE);
    }
}
