package org.galaxylib.api.system.loot.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;

import java.util.List;

public record LootPoolDefinition(
        String name,
        int baseWeight,
        List<LootEntryDefinition> entries
) {

    public static final Codec<LootPoolDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(LootPoolDefinition::name),
                    Codec.INT.fieldOf("base_weight").forGetter(LootPoolDefinition::baseWeight),
                    LootEntryDefinition.CODEC.listOf().fieldOf("entries").forGetter(LootPoolDefinition::entries)
            ).apply(instance, LootPoolDefinition::new)
    );

    public WeightedRandomList<WeightedEntry.Wrapper<LootEntryDefinition>> entriesAsWeightedList() {
        return WeightedRandomList.create(entries.stream()
                .filter(entry -> entry.weight() > 0)
                .map(entry -> WeightedEntry.wrap(entry, entry.weight()))
                .toList());
    }

    public WeightedEntry.Wrapper<LootPoolDefinition> toWeighted() {
        return WeightedEntry.wrap(this, baseWeight);
    }
}
