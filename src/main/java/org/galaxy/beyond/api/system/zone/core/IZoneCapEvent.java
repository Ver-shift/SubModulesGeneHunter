package org.galaxy.beyond.api.system.zone.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import org.galaxy.beyond.api.system.zone.ZoneType;

public interface IZoneCapEvent {

    default void levelTick(ServerLevel level, ZoneType zoneType) {}

    default void mobTick(Mob mob, ZoneType zoneType) {}

    default void playerTick(ServerPlayer player, ZoneType zoneType) {}

    /**
     * 玩家从其他区域进入本区域时触发。
     * @param player 玩家
     * @param from   来源区域
     * @param to     目标区域（即本区域）
     */
    default void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {}

    default void playerRightClickBlock(ServerPlayer player, Block block) {}
}
