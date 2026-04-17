package com.pz.beyond.api.system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.zone.zones.PlayerZoneData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@ToString(exclude = {"playerZoneData", "player"})
@Setter
public class BeyondData {

    public static final String PLAYER_ID = "player_id";
    public static final String CLIENT_PLAYER_ID = "client_player_id";
    public static final String PLAYER_ZONE_DATA = "player_zone_data";

    // CODEC - 使用延迟初始化的 getter
    public static final Codec<BeyondData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf(PLAYER_ID).forGetter(data -> data.playerId != null ? data.playerId.toString() : ""),
                    PlayerZoneData.CODEC.optionalFieldOf(PLAYER_ZONE_DATA, new PlayerZoneData()).forGetter(BeyondData::getPlayerZoneData)
            ).apply(instance, (playerIdStr, zoneData) -> {
                UUID playerId = playerIdStr.isEmpty() ? null : UUID.fromString(playerIdStr);
                BeyondData data = new BeyondData(playerId);
                data.playerZoneData = zoneData;
                data.playerZoneData.setBeyondData(data);
                return data;
            })
    );

    // STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, BeyondData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            data -> data.playerId != null ? data.playerId.toString() : "",
            PlayerZoneData.STREAM_CODEC,
            BeyondData::getPlayerZoneData,
            (playerIdStr, zoneData) -> {
                UUID playerId = playerIdStr.isEmpty() ? null : UUID.fromString(playerIdStr);
                BeyondData data = new BeyondData(playerId);
                data.playerZoneData = zoneData;
                data.playerZoneData.setBeyondData(data);
                return data;
            }
    );

    @Nullable
    private UUID playerId = null;
    private int clientPlayerId = -1;

    private PlayerZoneData playerZoneData;

    // 服务端持有Player对象（不序列化）
    // 使用自定义 getter/setter 确保同步到 PlayerZoneData
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient ServerPlayer player;

    public BeyondData() {}

    public BeyondData(@Nullable UUID playerId) {
        this.playerId = playerId;
    }

    // 服务端发送用
    public BeyondData(@Nullable ServerPlayer player) {
        this(player == null ? null : player.getUUID());
        this.player = player;
    }

    public PlayerZoneData getPlayerZoneData() {
        if (playerZoneData == null) {
            playerZoneData = new PlayerZoneData(this);
        }
        return playerZoneData;
    }

    /**
     * 获取玩家引用
     * 如果 player 为 null 且 playerId 有效，从服务器获取玩家
     */
    @Nullable
    public ServerPlayer getPlayer() {
        if (player == null && playerId != null) {
            // 尝试从服务器获取玩家
            MinecraftServer server = getCurrentServer();
            if (server != null) {
                player = server.getPlayerList().getPlayer(playerId);
            }
        }
        return player;
    }

    /**
     * 获取当前服务器实例
     * 优先从 player 获取，否则使用 NeoForge 静态方法
     */
    @Nullable
    private MinecraftServer getCurrentServer() {
        // 优先从 player 获取（更可靠）
        if (player != null) {
            return player.getServer();
        }
        // 备用：NeoForge 静态方法（服务器运行时可用）
        return ServerLifecycleHooks.getCurrentServer();
    }

    /**
     * 设置玩家引用（同时同步到嵌套的 PlayerZoneData）
     */
    public void setPlayer(ServerPlayer player) {
        this.player = player;
        this.playerId = player != null ? player.getUUID() : null;
        // 自动同步到嵌套数据
        if (playerZoneData != null) {
            playerZoneData.setPlayer(player);
        }
    }

    /**
     * 清除玩家引用（用于玩家下线时释放资源）
     */
    public void clearPlayer() {
        this.player = null;
        if (playerZoneData != null) {
            playerZoneData.clearPlayer();
        }
    }

    /**
     * 检查数据是否已初始化
     */
    public boolean isInitialized() {
        return player != null && playerId != null;
    }
}
