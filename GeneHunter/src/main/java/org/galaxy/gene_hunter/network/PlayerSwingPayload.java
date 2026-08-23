package org.galaxy.gene_hunter.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.effect.AttackSlashHandler;

public record PlayerSwingPayload() implements CustomPacketPayload {
    public static final Type<PlayerSwingPayload> TYPE = new Type<>(GeneHunter.asResource("player_swing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSwingPayload> STREAM_CODEC = StreamCodec.unit(new PlayerSwingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerSwingPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AttackSlashHandler.startSlash(player, null);
            }
        });
    }
}
