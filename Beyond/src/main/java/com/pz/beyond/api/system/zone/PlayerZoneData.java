package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.event.custom.PlayerChangeZoneEvent;
import com.pz.beyond.api.init.BeyondZoneInit;
import lombok.Data;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 玩家当前所在的区域。
 */
@Data
public class PlayerZoneData {

    public static final String CURRENT_ZONE_ID = "current_zone_id";


    @Getter
    private ZoneType currentZone = BeyondZoneInit.EMPTY;
    @Getter
    private ServerPlayer player;

    public PlayerZoneData(ServerPlayer player) {
        this.player = player;
    }
    public PlayerZoneData() {
        this.player = null;
    }

    // 工厂方法专用构造
    private PlayerZoneData(ZoneType currentZone) {
        this.currentZone = currentZone == null ? BeyondZoneInit.EMPTY : currentZone;
    }

    public void setCurrentZone(ZoneType newZone) {
        ZoneType safeZone = newZone == null ? BeyondZoneInit.EMPTY : newZone;
        if (player!=null){
            // 仅在服务端发布事件并允许监听器修改目标区域
            PlayerChangeZoneEvent event = new PlayerChangeZoneEvent(player, this.currentZone, safeZone);
            var postEvent = NeoForge.EVENT_BUS.post(event);
            ZoneType eventZone = postEvent.getNewZone();
            this.currentZone = eventZone == null ? BeyondZoneInit.EMPTY : eventZone;
        }else {
            this.currentZone = safeZone;
        }
    }

    public static final Codec<PlayerZoneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ZoneType.CODEC.fieldOf(CURRENT_ZONE_ID).forGetter(PlayerZoneData::getCurrentZone)
            ).apply(instance, PlayerZoneData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerZoneData> STREAM_CODEC = StreamCodec.composite(
            ZoneType.STREAM_CODEC,
            PlayerZoneData::getCurrentZone,
            PlayerZoneData::new
    );



}
