package com.pz.beyond.api.system.zone.zones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.event.custom.PlayerChangeZoneEvent;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.system.BeyondPlayerData;
import com.pz.beyond.api.system.zone.ZoneType;
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
public class PlayerZoneData {

    public static final String CURRENT_ZONE_ID = "current_zone_id";


    @Nullable
    private ZoneType currentZone;
    private ServerPlayer player;

    public PlayerZoneData(ServerPlayer player) {
        this.player = player;
    }
    public PlayerZoneData() {
        this.player = null;
    }

    // 工厂方法专用构造
    private PlayerZoneData(ZoneType currentZone) {
        this.currentZone = currentZone;
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
