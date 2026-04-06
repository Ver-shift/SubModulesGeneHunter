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
import org.galaxylib.api.system.loot.data.LootTableGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class PlayerLootTableData {

    private Map<ILootType<?>, LootTableGroup> lootTableGroups = new HashMap<>();
    private ServerPlayer player;
    private int playerId = -1;

    public PlayerLootTableData() {
    }

    public LootTableGroup getLootTableGroup(java.util.function.Supplier<ILootType<?>> lootType) {
        return lootTableGroups.computeIfAbsent(lootType.get(), k -> new LootTableGroup());
    }




    // Only player-selected loot groups are serialized on attachment.
    public static final Codec<PlayerLootTableData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("loot_type_ids").forGetter(data ->
                            data.lootTableGroups.keySet().stream()
                                    .map(type -> GalaxyLibLootTypeInit.LOOT_TYPE_REGISTRY.getKey(type))
                                    .toList()
                    )
            ).apply(instance, ids -> {
                PlayerLootTableData data = new PlayerLootTableData();
                ids.forEach(id -> {
                    ILootType<?> type = GalaxyLibLootTypeInit.getLootTypeById(id);
                    if (type != null) {
                        data.lootTableGroups.put(type, new LootTableGroup());
                    }
                });
                return data;
            })
    );

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
