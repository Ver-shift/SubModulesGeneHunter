package org.biotech.component;

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
 * 多词条组件 - 支持一个基因携带多个词条
 */
@Data
public class TraitComp {

    private List<ITrait> traits = new ArrayList<>();

    public TraitComp() {}

    public TraitComp(List<ITrait> traits) {
        this.traits = traits != null ? new ArrayList<>(traits) : new ArrayList<>();
    }

    public void addTrait(ITrait trait) {
        if (trait != null && trait.getId() != null && !ITrait.EMPTY_TRAIT_ID.equals(trait.getId()) && !traits.contains(trait)) {
            traits.add(trait);
        }
    }

    public void removeTrait(ITrait trait) {
        traits.remove(trait);
    }

    public boolean hasTrait(ITrait trait) {
        return traits.contains(trait);
    }

    public boolean isEmpty() {
        return traits.isEmpty();
    }

    public int size() {
        return traits.size();
    }

    public static TraitComp of(ITrait... traits) {
        TraitComp comp = new TraitComp();
        for (ITrait trait : traits) {
            comp.addTrait(trait);
        }
        return comp;
    }

    /**
     * 从列表快速创建
     */
    public static TraitComp fromList(List<ITrait> traits) {
        return new TraitComp(traits);
    }

    /**
     * 创建空组件
     */
    public static TraitComp empty() {
        return new TraitComp();
    }

    /**
     * 创建包含指定数量随机词条的组件
     */
    public static TraitComp random(int count) {
        TraitComp comp = new TraitComp();
        // 从 TraitInit 获取随机词条
        var allTraits = BiotechTraitInit.getAllTraits();
        if (allTraits.isEmpty()) return comp;

        java.util.Collections.shuffle(allTraits);
        for (int i = 0; i < Math.min(count, allTraits.size()); i++) {
            comp.addTrait(allTraits.get(i));
        }
        return comp;
    }

    // CODEC
    public static final Codec<TraitComp> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ITrait.CODEC.listOf().fieldOf("traits").forGetter(TraitComp::getTraits)
        ).apply(instance, TraitComp::new)
    );

    // STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, TraitComp> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, ITrait.STREAM_CODEC),
        TraitComp::getTraits,
        TraitComp::new
    );
}
