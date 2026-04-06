package org.biotech.api.system.trait;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.biotech.api.init.BiotechTraitInit;
import org.biotech.api.system.trait.core.ITrait;

import java.util.ArrayList;
import java.util.List;

/**
 * 词条表数据 - 存储所有可用词条
 */
@Data
public class TraitTableData {

    private List<ITrait> allTraits = new ArrayList<>();

    public TraitTableData() {
        this.allTraits = BiotechTraitInit.getAllTraits();
    }

    public TraitTableData(List<ITrait> allTraits) {
        this.allTraits = allTraits != null ? new ArrayList<>(allTraits) : new ArrayList<>();
    }

    // CODEC
    public static final Codec<TraitTableData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ITrait.CODEC.listOf().fieldOf("traits").forGetter(TraitTableData::getAllTraits)
        ).apply(instance, TraitTableData::new)
    );

    // STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, TraitTableData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, ITrait.STREAM_CODEC),
        TraitTableData::getAllTraits,
        TraitTableData::new
    );
}
