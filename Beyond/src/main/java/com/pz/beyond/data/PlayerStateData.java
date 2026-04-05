package com.pz.beyond.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerStateData {

private PlayerState playerState = PlayerState.INSIDE_SAFETY_ZONE;


    public PlayerStateData() {
    }

    public PlayerStateData(PlayerState playerState) {
        this.playerState = playerState;
    }

    public static final Codec<PlayerStateData> CODEC = RecordCodecBuilder.create(i->i.group(
            PlayerState.CODEC.fieldOf("playerState").forGetter(data->data.playerState))
            .apply(i,PlayerStateData::new)
    );


    public PlayerState getPlayerState() {
        return this.playerState;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
    }

    /**
     * 用来存储玩家状态
     * <ul>
     *     <li>{@link #INSIDE_SAFETY_ZONE} - 在安全区内</li>
     *     <li>{@link #IN_GAME} - 在游戏中</li>
     * </ul>
     */
    public enum PlayerState implements StringRepresentable {
        INSIDE_SAFETY_ZONE("insideSafetyZone"),
        IN_GAME("inGame");

        private final String name;

        PlayerState(String name) {
            this.name = name;
        }
        public static final Codec<PlayerState> CODEC =
                StringRepresentable.fromValues(PlayerState::values);
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }


}
