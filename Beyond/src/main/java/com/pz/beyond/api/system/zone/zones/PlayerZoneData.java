package com.pz.beyond.api.system.zone.zones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.event.custom.PlayerChangeZoneEvent;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.BeyondData;
import com.pz.beyond.api.system.zone.AbstractZone;
import com.pz.beyond.api.system.zone.LevelZoneData;
import com.pz.beyond.api.system.zone.ZoneData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * 玩家当前所在的区域。
 */
@Data
@ToString(exclude = {"beyondData", "player"})
public class PlayerZoneData {

    public static final String CURRENT_ZONE_ID = "current_zone_id";

    // CODEC - 只序列化 currentZone 的 ID
    public static final Codec<PlayerZoneData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf(CURRENT_ZONE_ID).forGetter(PlayerZoneData::getCurrentZoneId)
            ).apply(instance, PlayerZoneData::new)
    );

    // STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerZoneData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            PlayerZoneData::getCurrentZoneId,
            PlayerZoneData::new
    );

    @Nullable
    private AbstractZone<?> currentZone;
    private BeyondData beyondData;
    // 使用自定义 getter，通过 beyondData 获取 player
    @Getter(AccessLevel.NONE)
    private transient ServerPlayer player;

    public PlayerZoneData() {}

    public PlayerZoneData(BeyondData beyondData) {
        this.beyondData = beyondData;
    }

    /**
     * 用于 CODEC 反序列化的构造函数
     */
    private PlayerZoneData(ResourceLocation zoneId) {
        this.currentZone = BeyondZoneInit.getZoneById(zoneId);
    }

    /**
     * 获取当前区域 ID（用于序列化）
     */
    private ResourceLocation getCurrentZoneId() {
        return BeyondZoneInit.getZoneId(currentZone);   
    }

    /**
     * 获取玩家引用
     * 优先使用本地缓存，否则从 beyondData 获取
     */
    @Nullable
    public ServerPlayer getPlayer() {
        if (player == null && beyondData != null) {
            player = beyondData.getPlayer();
        }
        return player;
    }

    /**
     * 检测并更新玩家当前所在的区域
     * 通过检测区块坐标确定区域类型
     * 
     * @param levelZoneData 维度区域数据
     * @return true 如果区域发生了变化
     */
    public boolean zoneTypeHandle(LevelZoneData levelZoneData) {
        ServerPlayer currentPlayer = getPlayer();
        if (currentPlayer == null || levelZoneData == null) {
            return false;
        }

        // 获取玩家当前区块
        long currentChunkKey = currentPlayer.chunkPosition().toLong();
        
        // 查找玩家当前所在的区域
        AbstractZone<?> newZone = levelZoneData.getZoneByChunkKey(currentChunkKey);
        
        // 检查区域是否变化
        if (!java.util.Objects.equals(currentZone, newZone)) {
            setCurrentZone(newZone);
            return true;
        }
        return false;
    }

    /**
     * 触发区域变更规则
     * 通过 LevelZoneData 获取 ZoneData，再获取监听器
     */
    public void handleRule(AbstractZone<?> oldZone, AbstractZone<?> newZone, ServerPlayer serverPlayer) {
        LevelZoneData levelZoneData = BeyondAPI.getZoneData(serverPlayer.level());

        // 通知旧区域的监听器：玩家离开
        if (oldZone != null) {
            ZoneData oldZoneData = levelZoneData.getZoneData().stream()
                    .filter(zd -> zd.getZone().equals(oldZone))
                    .findFirst().orElse(null);
            if (oldZoneData != null) {
                oldZoneData.getListeners().forEach(listener ->
                        listener.getRule().playerChangeZone(serverPlayer, oldZone, newZone));
            }
        }
        // 通知新区域的监听器：玩家进入
        if (newZone != null && newZone != oldZone) {
            ZoneData newZoneData = levelZoneData.getZoneData().stream()
                    .filter(zd -> zd.getZone().equals(newZone))
                    .findFirst().orElse(null);
            if (newZoneData != null) {
                newZoneData.getListeners().forEach(listener ->
                        listener.getRule().playerChangeZone(serverPlayer, oldZone, newZone));
            }
        }
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public void setCurrentZone(AbstractZone<?> newZone) {
        AbstractZone<?> oldZone = this.currentZone;
        ServerPlayer currentPlayer = getPlayer();
        PlayerChangeZoneEvent event = new PlayerChangeZoneEvent(currentPlayer, oldZone, newZone);
        this.currentZone = event.getNewZone();
        handleRule(oldZone, this.currentZone, currentPlayer);
    }

    /**
     * 清除玩家引用（用于玩家下线时释放资源）
     */
    public void clearPlayer() {
        this.player = null;
    }

    /**
     * 检查数据是否已初始化
     */
    public boolean isInitialized() {
        return getPlayer() != null;
    }
}
