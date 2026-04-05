package com.pz.beyond.network;

import com.pz.beyond.Beyond;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SafeZonePayload(
        int centerX,
        int centerZ,
        int radius
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SafeZonePayload> TYPE =new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Beyond.MODID,"safe_zone_payload"));
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf,SafeZonePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SafeZonePayload::centerX,
            ByteBufCodecs.INT,
            SafeZonePayload::centerZ,
            ByteBufCodecs.INT,
            SafeZonePayload::radius,
            SafeZonePayload::new
    );
}
