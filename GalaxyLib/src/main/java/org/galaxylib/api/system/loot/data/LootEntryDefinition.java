package org.galaxylib.api.system.loot.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record LootEntryDefinition(
        ResourceLocation id,
        int count,
        int weight,
        Optional<String> nbt
) {

    public static final Codec<LootEntryDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(LootEntryDefinition::id),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(LootEntryDefinition::count),
                    Codec.INT.fieldOf("weight").forGetter(LootEntryDefinition::weight),
                    Codec.STRING.optionalFieldOf("nbt").forGetter(LootEntryDefinition::nbt)
            ).apply(instance, LootEntryDefinition::new)
    );
}
