package org.biotech.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.biotech.api.system.gene.inventory.PlayerGeneInventoryData;
import org.biotech.api.system.loot.PlayerLootTableData;
import org.biotech.api.system.merge.MergeData;

/**
 * 玩家基因数据 - 聚合所有基因相关数据（延迟初始化）
 */
@Data
public class GeneData {


    private int playerId = -1;
    private PlayerLootTableData playerLootTableData;
    private PlayerGeneInventoryData playerGeneInventoryData;
    private MergeData mergeData;
    // 服务端持有Player对象（不序列化）

    private transient ServerPlayer player;

    public GeneData() {}

    public GeneData(int playerId) {
        this.playerId = playerId;
    }

    // 服务端发送用
    public GeneData(ServerPlayer player) {
        this(player == null ? -1 : player.getId());
        this.player = player;
    }



    public PlayerLootTableData getPlayerLootTableData() {
        if (playerLootTableData == null) {
            playerLootTableData = new PlayerLootTableData();
        }
        return playerLootTableData;
    }

    public PlayerGeneInventoryData getPlayerGeneInventoryData() {
        if (playerGeneInventoryData == null) {
            playerGeneInventoryData = new PlayerGeneInventoryData();
        }
        return playerGeneInventoryData;
    }

    public MergeData getMergeData() {
        if (mergeData == null) {
            mergeData = new MergeData();
        }
        return mergeData;
    }



    // CODEC - 使用延迟初始化的 getter，确保不会返回 null
    public static final Codec<GeneData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("player_id").forGetter(GeneData::getPlayerId),
            PlayerLootTableData.CODEC.optionalFieldOf("loot_table", new PlayerLootTableData()).forGetter(GeneData::getPlayerLootTableData),
            PlayerGeneInventoryData.CODEC.optionalFieldOf("gene_inventory", new PlayerGeneInventoryData()).forGetter(GeneData::getPlayerGeneInventoryData),
            MergeData.CODEC.optionalFieldOf("merge", new MergeData()).forGetter(GeneData::getMergeData)
        ).apply(instance, (playerId, lootTable, geneInventory, merge) -> {
            GeneData data = new GeneData(playerId);
            data.playerLootTableData = lootTable;
            data.playerGeneInventoryData = geneInventory;
            data.mergeData = merge;
            return data;
        })
    );

    // STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, GeneData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        GeneData::getPlayerId,
        PlayerLootTableData.STREAM_CODEC,
        GeneData::getPlayerLootTableData,
        PlayerGeneInventoryData.STREAM_CODEC,
        GeneData::getPlayerGeneInventoryData,
        MergeData.STREAM_CODEC,
        GeneData::getMergeData,
        (playerId, lootTable, geneInventory, merge) -> {
            GeneData data = new GeneData(playerId);
            data.playerLootTableData = lootTable;
            data.playerGeneInventoryData = geneInventory;
            data.mergeData = merge;
            return data;
        }
    );
}
