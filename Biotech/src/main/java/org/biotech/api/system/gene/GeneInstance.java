package org.biotech.api.system.gene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** A stack-local reference to an immutable datapack gene definition. */
@Data
public class GeneInstance {
    public static final GeneInstance EMPTY = new GeneInstance(null);

    @Nullable private final ResourceLocation geneId;
    private int count = 1;

    public GeneInstance(@Nullable ResourceLocation geneId) {
        this.geneId = geneId;
    }

    public boolean isEmpty() { return geneId == null; }

    public static final Codec<GeneInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("gene").forGetter(value -> Optional.ofNullable(value.geneId)),
            Codec.INT.optionalFieldOf("count", 1).forGetter(GeneInstance::getCount)
    ).apply(instance, (id, count) -> {
        GeneInstance value = new GeneInstance(id.orElse(null));
        value.setCount(count);
        return value;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, GeneInstance> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GeneInstance decode(RegistryFriendlyByteBuf buffer) {
            ResourceLocation id = ByteBufCodecs.BOOL.decode(buffer) ? buffer.readResourceLocation() : null;
            GeneInstance value = new GeneInstance(id);
            value.setCount(ByteBufCodecs.VAR_INT.decode(buffer));
            return value;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, GeneInstance value) {
            ByteBufCodecs.BOOL.encode(buffer, value.geneId != null);
            if (value.geneId != null) buffer.writeResourceLocation(value.geneId);
            ByteBufCodecs.VAR_INT.encode(buffer, value.count);
        }
    };
}
