package org.galaxy.gene_hunter.api.system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.galaxy.gene_hunter.api.system.choice.ChoiceHolderData;

@Data
public class GeneHunterData {

    private ChoiceHolderData choiceHolderData;
    private transient ServerPlayer player;
    private int playerId = -1;

    public GeneHunterData() {}

    // 服务端发送用
    public GeneHunterData(ServerPlayer player) {
        this(player == null ? -1 : player.getId());
        this.player = player;
    }

    public GeneHunterData(int playerId) {
        this.playerId = playerId;
    }

    public GeneHunterData(int playerId, ChoiceHolderData choiceHolderData) {
        this.playerId = playerId;
        this.choiceHolderData = choiceHolderData;
    }

    public ChoiceHolderData getChoiceHolderData() {
        if (choiceHolderData == null) {
            choiceHolderData = new ChoiceHolderData();
        }
        return choiceHolderData;
    }
    /**
     * 检查数据是否已初始化
     */
    public boolean isInitialized() {
        return player != null && playerId != -1;
    }

    // CODEC - 使用延迟初始化的 getter，确保不会返回 null
    public static final Codec<GeneHunterData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("player_id").forGetter(GeneHunterData::getPlayerId),
            ChoiceHolderData.CODEC.optionalFieldOf("choice_holder", new ChoiceHolderData()).forGetter(GeneHunterData::getChoiceHolderData)
        ).apply(instance, (playerId, choiceHolder) -> {
            GeneHunterData data = new GeneHunterData(playerId);
            data.choiceHolderData = choiceHolder;
            return data;
        })
    );

    // STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, GeneHunterData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        GeneHunterData::getPlayerId,
        ChoiceHolderData.STREAM_CODEC,
        GeneHunterData::getChoiceHolderData,
        (playerId, choiceHolder) -> {
            GeneHunterData data = new GeneHunterData(playerId);
            data.choiceHolderData = choiceHolder;
            return data;
        }
    );
}
