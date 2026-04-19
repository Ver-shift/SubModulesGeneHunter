package com.pz.beyond.api.system.structure;

import com.mojang.serialization.Codec;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

@Data
public class PlayerLoginData {

    private boolean firstLogin = true;
    private ServerPlayer player;

    public PlayerLoginData(ServerPlayer player) {
        this.player = player;
    }

    public PlayerLoginData() {
        this.player = null;
    }

    private PlayerLoginData(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public static final Codec<PlayerLoginData> CODEC = Codec.BOOL.xmap(PlayerLoginData::new, PlayerLoginData::isFirstLogin);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerLoginData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            PlayerLoginData::isFirstLogin,
            PlayerLoginData::new
    );
}
