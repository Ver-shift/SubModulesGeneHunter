package com.pz.beyond.safeZoneRule.impl;

import com.pz.beyond.data.PlayerStateData;
import com.pz.beyond.registry.ModAttachments;
import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import com.pz.beyond.safeZoneRule.SafeZoneRule;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@SafeZoneRule
public class Invincible implements ISafeZoneRuleListener {

    @Override
    public void invincible(LivingDamageEvent.Pre event) {

        if (event.getEntity() instanceof ServerPlayer serverPlayer){
            PlayerStateData data = serverPlayer.getData(ModAttachments.PLAYER_STATE);
            if (data.getPlayerState() == PlayerStateData.PlayerState.INSIDE_SAFETY_ZONE) event.setNewDamage(0);
        }

    }
}
