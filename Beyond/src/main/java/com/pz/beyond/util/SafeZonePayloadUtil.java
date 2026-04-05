package com.pz.beyond.util;

import com.pz.beyond.data.SafeZoneData;
import com.pz.beyond.network.SafeZonePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class SafeZonePayloadUtil {

    /**
     * 给单个玩家发送数据包
     * @param player 要接收的玩家
     * @param data 发送的数据包
     */
    public static void safeZoneToPlayer(ServerPlayer player, SafeZoneData data) {
        SafeZonePayload payload = new SafeZonePayload(data.getCenterX(), data.getCenterZ(), data.getRadius());
        player.connection.send(payload);
    }

    /**
     * 广播安全区数据到世界内的所有玩家
     * @param serverLevel 当前的世界
     * @param data 发送的数据包
     */
    public static void SafeZoneToAll(ServerLevel serverLevel, SafeZoneData data) {
        SafeZonePayload payload = new SafeZonePayload(data.getCenterX(), data.getCenterZ(), data.getRadius());

        PacketDistributor.sendToPlayersInDimension(serverLevel,payload);
    }
}
