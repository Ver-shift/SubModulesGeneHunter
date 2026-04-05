package com.pz.beyond.safeZoneRule.impl;

import com.pz.beyond.data.SafeZoneData;
import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import com.pz.beyond.safeZoneRule.SafeZoneRule;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
@SafeZoneRule
public class NoEat implements ISafeZoneRuleListener {
    @Override
    public void onSafeZoneTick(SafeZoneContext context) {
        ServerPlayer player = context.player();
        FoodData foodData = player.getFoodData();
        if (player.tickCount % 20 == 0) {
            foodData.setFoodLevel(20);
            foodData.setSaturation(5.0f);
        }

    }
}
