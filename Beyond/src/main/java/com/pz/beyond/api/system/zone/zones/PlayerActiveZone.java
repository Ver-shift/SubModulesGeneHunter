package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * 玩家可活动区域
 */
public class PlayerActiveZone extends AbstractZone<Void> {

    public static final ResourceLocation PLAYER_ACTIVE_ZONE = Beyond.asResource("player_active_zone");

    public PlayerActiveZone() {
        super(PLAYER_ACTIVE_ZONE);
    }


    @Override
    public void initialize(List<RuleData> listeners, ServerLevel level) {

    }
}
