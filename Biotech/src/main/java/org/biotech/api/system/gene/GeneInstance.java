package org.biotech.api.system.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.core.component.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.biotech.api.system.gene.core.GeneRarity;
import org.biotech.api.system.gene.core.IGene;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Data
public class GeneInstance implements MutableDataComponentHolder{

    public static final GeneInstance EMPTY = new GeneInstance(null);

    private IGene gene;
    private int count = 1; //最高64堆叠
    private final PatchedDataComponentMap components = new PatchedDataComponentMap(DataComponentMap.EMPTY);

    public GeneInstance(IGene gene) {
        this.gene = gene;
        if (gene != null) {
            applyComponents(gene.getConfigBuilder().build());
        }
    }

    // CODEC - gene 字段可为 null（返回 EMPTY 常量而非新实例）
    public static final Codec<GeneInstance> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            IGene.CODEC.optionalFieldOf("gene").forGetter(i -> Optional.ofNullable(i.getGene())),
            Codec.INT.fieldOf("count").forGetter(GeneInstance::getCount),
            DataComponentPatch.CODEC.fieldOf("components").forGetter(i -> i.components.asPatch())
        ).apply(instance, (g, c, p) -> {
            IGene gene = g.orElse(null);
            // 如果 gene 为 null，返回 EMPTY 常量
            if (gene == null) {
                return GeneInstance.EMPTY;
            }
            GeneInstance instance1 = new GeneInstance(gene);
            instance1.setCount(c);
            instance1.applyComponents(p);
            return instance1;
        })
    );

    // STREAM_CODEC - 支持 null gene（EMPTY 实例）
    public static final StreamCodec<RegistryFriendlyByteBuf, GeneInstance> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, GeneInstance instance) {
            // 写入 gene（支持 null）
            IGene.STREAM_CODEC.encode(buf, instance.getGene());
            // 写入 count
            ByteBufCodecs.VAR_INT.encode(buf, instance.getCount());
            // 写入 components
            DataComponentPatch.STREAM_CODEC.encode(buf, instance.components.asPatch());
        }

        @Override
        public GeneInstance decode(RegistryFriendlyByteBuf buf) {
            IGene gene = IGene.STREAM_CODEC.decode(buf);
            int count = ByteBufCodecs.VAR_INT.decode(buf);
            DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
            GeneInstance instance = new GeneInstance(gene);
            instance.setCount(count);
            instance.applyComponents(patch);
            return instance;
        }
    };

    


    public GeneRarity getRarity(){
        return GeneRarity.fromMinecraftRarity(components.get(DataComponents.RARITY));
    }







    @Override
    public @Nullable <T> T set(DataComponentType<? super T> componentType, @Nullable T value) {
        return this.components.set(componentType, value);
    }

    @Override
    public @Nullable <T> T remove(DataComponentType<? extends T> componentType) {
        return this.components.remove(componentType);
    }

    @Override
    public void applyComponents(DataComponentPatch patch) {
        this.components.applyPatch(patch);
    }

    @Override
    public void applyComponents(DataComponentMap components) {
        this.components.setAll(components);
    }

    @Override
    public @NotNull DataComponentMap getComponents() {
        return this.components;
    }




}
