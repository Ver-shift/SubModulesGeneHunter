package com.pz.beyond.api.system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.zone.zones.PlayerActiveZone;
import com.pz.beyond.api.system.zone.zones.PlayerZoneData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 玩家Beyond模块数据 - 聚合所有Beyond相关数据（延迟初始化）
 */
@Data
public class BeyondPlayerData {

    public static final String PLAYER_UUID = "player_uuid";
    public static final String PLAYER_ZONE_DATA = "player_zone_data";

    private ServerPlayer serverPlayer;
    private UUID playerUUID;

    private PlayerZoneData playerZoneData;


    //server
    public BeyondPlayerData(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
        this.playerUUID = serverPlayer.getUUID();
    }

    //client
    public BeyondPlayerData(){
        if (Minecraft.getInstance().player != null) {
            this.playerUUID = Minecraft.getInstance().player.getUUID();
            this.serverPlayer = null;
        }
    }


    public ServerPlayer getPlayer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        if (serverPlayer == null) {
            return server.getPlayerList().getPlayer(playerUUID);
        }else {
            return serverPlayer;
        }
    }

    public PlayerZoneData getPlayerZoneData() {
        if (playerZoneData == null) {
            if (serverPlayer == null){
                playerZoneData = new PlayerZoneData();
            }else {
                playerZoneData = new PlayerZoneData(serverPlayer);
            }
        }
        return playerZoneData;
    }


    private BeyondPlayerData(UUID uuid, PlayerZoneData playerZoneData) {
        this.playerUUID = uuid;
        this.playerZoneData = playerZoneData;
    }

    public static final Codec<BeyondPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf(PLAYER_UUID).forGetter(BeyondPlayerData::getPlayerUUID),
        PlayerZoneData.CODEC.fieldOf(PLAYER_ZONE_DATA).forGetter(BeyondPlayerData::getPlayerZoneData)
    ).apply(instance, BeyondPlayerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BeyondPlayerData> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        BeyondPlayerData::getPlayerUUID,
        PlayerZoneData.STREAM_CODEC,
        BeyondPlayerData::getPlayerZoneData,
        BeyondPlayerData::new
    );

}
