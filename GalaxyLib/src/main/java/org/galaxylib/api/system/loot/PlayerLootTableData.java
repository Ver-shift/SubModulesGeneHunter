package org.galaxylib.api.system.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.GeneLootTableData;
import org.galaxylib.api.system.loot.data.LootTableGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Data
public class PlayerLootTableData {


    private Map<ResourceLocation, GeneLootTableData> dataPackTables = Map.of();

    private Map<ILootType<?>,LootTableGroup> lootTableGroups = new HashMap<>();
    private ServerPlayer player;
    private int playerId = -1;


    public PlayerLootTableData() {
        // 默认构造函数
    }

    /**
     * 根据战利品类型获取对应的 LootTableGroup
     */
    public LootTableGroup getLootTableGroup(java.util.function.Supplier<ILootType<?>> lootType) {
        return lootTableGroups.computeIfAbsent(lootType.get(), k -> new LootTableGroup());
    }









    // CODEC - 序列化（只序列化 lootTableGroups 的 key，dataPackTables 从数据包重新加载）
    public static final Codec<PlayerLootTableData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("loot_type_ids").forGetter(data ->
                data.lootTableGroups.keySet().stream()
                    .map(type -> GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY.getKey(type))
                    .toList()
            )
        ).apply(instance, ids -> {
            PlayerLootTableData data = new PlayerLootTableData();
            // 初始化空的 LootTableGroup（实际数据从数据包合并）
            ids.forEach(id -> {
                ILootType<?> type = GalaxyLibLootTypeInit.getLootTypeById(id);
                if (type != null) {
                    data.lootTableGroups.put(type, new LootTableGroup());
                }
            });
            return data;
        })
    );

    // STREAM_CODEC - 网络同步（只同步 lootTableGroups 的 key）
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerLootTableData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC),
        data -> data.lootTableGroups.keySet().stream()
            .map(type -> GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY.getKey(type))
            .toList(),
        ids -> {
            PlayerLootTableData data = new PlayerLootTableData();
            ids.forEach(id -> {
                ILootType<?> type = GalaxyLibLootTypeInit.getLootTypeById(id);
                if (type != null) {
                    data.lootTableGroups.put(type, new LootTableGroup());
                }
            });
            return data;
        }
    );
}
