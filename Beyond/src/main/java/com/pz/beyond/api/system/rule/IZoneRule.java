package com.pz.beyond.api.system.rule;

import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface IZoneRule {



    /**
     * 玩家区域内的行为
     * @param level
     */
    default void levelTick(ServerLevel level, AbstractZone<?> zoneType) {

    }

    /**
     * 玩家在区域内的行为
     * @param player
     */
    default void playerTick(ServerPlayer player, AbstractZone<?> zoneType) {

    }

    /**
     * 切换区域，一定是从其他区域进入这个区域
     */
    default void playerChangeZone(ServerPlayer player, AbstractZone<?> from, AbstractZone<?> to){

    }


}
