package org.biotech.api.system.loot.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 战利品池数据 - 数据包加载用
 * <p>
 * 对应 sword.json 中的 pools 数组元素
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LootPoolData {

    private String name;                    // 池名称（如"普通物品"）
    private int baseWeight;                 // 池的基础权重
    private List<Entry> entries;            // 条目列表

    public static final Codec<LootPoolData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("name").forGetter(LootPoolData::getName),
            Codec.INT.fieldOf("base_weight").forGetter(LootPoolData::getBaseWeight),
            Entry.CODEC.listOf().fieldOf("entries").forGetter(LootPoolData::getEntries)
        ).apply(instance, LootPoolData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, LootPoolData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        LootPoolData::getName,
        ByteBufCodecs.VAR_INT,
        LootPoolData::getBaseWeight,
        ByteBufCodecs.collection(ArrayList::new, Entry.STREAM_CODEC),
        LootPoolData::getEntries,
        LootPoolData::new
    );

    /**
     * 战利品条目 - 内部类
     * <p>
     * 对应 sword.json 中的 entries 数组元素
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {

        private ResourceLocation id;    // 物品/词条 ID
        private int count = 1;          // 数量（默认1）
        private int weight;             // 权重

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Entry::getId),
                Codec.INT.optionalFieldOf("count", 1).forGetter(Entry::getCount),
                Codec.INT.fieldOf("weight").forGetter(Entry::getWeight)
            ).apply(instance, Entry::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            Entry::getId,
            ByteBufCodecs.VAR_INT,
            Entry::getCount,
            ByteBufCodecs.VAR_INT,
            Entry::getWeight,
            Entry::new
        );
    }
}
