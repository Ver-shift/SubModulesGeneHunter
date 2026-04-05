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
import java.util.Optional;

/**
 * 单个基因战利品表数据 - 数据包加载用
 * <p>
 * 对应 sword.json 的完整结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneLootTableData {

    private ResourceLocation identify;      // 表ID（如 biotech:sword）
    private Optional<String> note;          // 注释说明（可选）
    private String lootType;                // 战利品类型（如 item, trait）
    private List<LootPoolData> pools;       // 战利品池列表

    public static final Codec<GeneLootTableData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("identify").forGetter(GeneLootTableData::getIdentify),
            Codec.STRING.optionalFieldOf("note").forGetter(GeneLootTableData::getNote),
            Codec.STRING.fieldOf("loot_type").forGetter(GeneLootTableData::getLootType),
            LootPoolData.CODEC.listOf().fieldOf("pools").forGetter(GeneLootTableData::getPools)
        ).apply(instance, GeneLootTableData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GeneLootTableData> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        GeneLootTableData::getIdentify,
        ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
        GeneLootTableData::getNote,
        ByteBufCodecs.STRING_UTF8,
        GeneLootTableData::getLootType,
        ByteBufCodecs.collection(ArrayList::new, LootPoolData.STREAM_CODEC),
        GeneLootTableData::getPools,
        GeneLootTableData::new
    );
}
