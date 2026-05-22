package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.zone.ZoneType;

public interface IRogueCap {

    /**
     * 区域最大规则等级
     * @return
     */
    default int getMaxLevel(){
        return 1;
    }


    default void levelTick(ServerLevel level, IRogueContext context) {

    }


    default void livingTick(LivingEntity entity, IRogueContext context) {

    }

    /**
     * 检查livingEntity切换了区域
     * @param entity
     * @param from
     * @param to
     * @param context
     */
    default void changeZone(LivingEntity entity,ZoneType from,ZoneType to,IRogueContext context) {

    }


    default void phaseEnter(ServerLevel level,Phase from,Phase to,IRogueContext context) {};

    default void phaseExit(ServerLevel level,Phase from,Phase to,IRogueContext context) {};

    default void phaseTick(ServerLevel level,Phase phase,IRogueContext context) {};

}
