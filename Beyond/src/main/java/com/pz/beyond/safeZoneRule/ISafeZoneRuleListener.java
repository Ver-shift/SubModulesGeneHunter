package com.pz.beyond.safeZoneRule;

import com.pz.beyond.data.SafeZoneData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

/**
 * 安全区规则监听器接口
 * <P>
 * 用于定义安全区内的各种规则处理逻辑
 */
public interface ISafeZoneRuleListener {

    /**
     * 在安全区内的每个tick
     * @param context
     */
    default void onSafeZoneTick(SafeZoneContext context){}

    /**
     * 在安全区外的每个tick
     * @param context
     */
    default void outSideSafeZoneTick(SafeZoneContext context){}

    /**
     * 无敌状态处理
     * @param event
     */
    default void invincible(LivingDamageEvent.Pre event){}

    /**
     * 禁止生成生物处理
     * @param event
     */
    default void onMobSpawn(MobSpawnEvent.PositionCheck event) {}

    record SafeZoneContext(ServerPlayer player){}

}
