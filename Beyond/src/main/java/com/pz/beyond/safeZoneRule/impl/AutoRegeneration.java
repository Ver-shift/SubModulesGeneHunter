package com.pz.beyond.safeZoneRule.impl;

import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import com.pz.beyond.safeZoneRule.SafeZoneRule;
import net.minecraft.server.level.ServerPlayer;

@SafeZoneRule
public class AutoRegeneration implements ISafeZoneRuleListener {

    @Override
    public void onSafeZoneTick(SafeZoneContext context) {
        ServerPlayer player = context.player();
        if (player.tickCount % 20 == 0 ) {
            player.heal(1.0f);
        }
    }
}
