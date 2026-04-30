package com.pz.beyond.api.system.zone;

import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.BeyondPlayerData;
import com.pz.beyond.api.system.rule.AbstractRule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ZoneManager {

    public void handleZoneRule(ServerLevel serverLevel) {
        LevelZoneData levelZoneData = BeyondAPI.getBeyondLevelData(serverLevel).getLevelZoneData();
        Set<ZoneType> tickedZones = new HashSet<>();

        for (ServerPlayer player : serverLevel.players()) {
            BeyondPlayerData playerData = BeyondAPI.getBeyondPlayerData(player);

            ZoneType oldZone = playerData.getPlayerZoneData().getCurrentZone();
            ZoneData oldZoneData = levelZoneData.getZoneData(oldZone);
            ZoneData newZoneData = levelZoneData.getZoneData(player.getOnPos());
            ZoneType newZone = newZoneData.getZone();

            if (tickedZones.add(newZone)) {
                dispatch(newZoneData, rule -> rule.levelTick(serverLevel, newZone));
            }

            dispatch(newZoneData, rule -> rule.playerTick(player, newZone));

            if (oldZone != newZone) {
                playerData.getPlayerZoneData().setCurrentZone(newZone);

                if (oldZone != BeyondZoneInit.EMPTY) {
                    dispatch(oldZoneData, rule -> rule.playerChangeZone(player, oldZone, newZone));
                    dispatch(newZoneData, rule -> rule.playerChangeZone(player, oldZone, newZone));
                }
            }
        }
    }

    public void handlePlayerRightClickBlock(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        ZoneData zoneData = BeyondAPI.getBeyondLevelData(level).getLevelZoneData().getZoneData(pos);
        Block block = level.getBlockState(pos).getBlock();
        dispatch(zoneData, rule -> rule.playerRightClickBlock(player, block));
    }

    public void handleMobTick(Mob mob) {
        if (mob == null) {
            return;
        }
        ServerLevel level = (ServerLevel) mob.level();
        ZoneData zoneData = BeyondAPI.getBeyondLevelData(level).getLevelZoneData().getZoneData(mob.blockPosition());
        dispatch(zoneData, rule -> rule.mobTick(mob, zoneData.getZone()));
    }

    /**
     * 统一的规则派发入口：遍历 {@link ZoneData#getListeners()} 并对每条规则执行 {@code action}。
     * 负责 null 防御，把散落的样板代码收敛到一处。
     */
    private static void dispatch(ZoneData zoneData, Consumer<AbstractRule> action) {
        if (zoneData == null || zoneData.getListeners() == null) {
            return;
        }
        zoneData.getListeners().forEach(listener -> action.accept(listener.getRule()));
    }
}
