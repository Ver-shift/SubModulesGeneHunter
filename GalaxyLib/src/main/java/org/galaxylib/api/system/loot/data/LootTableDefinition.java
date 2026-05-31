package org.galaxylib.api.system.loot.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;

import java.util.List;
import java.util.Optional;

public record LootTableDefinition(
        ResourceLocation identify,
        Optional<String> note,
        ResourceLocation lootType,
        List<LootPoolDefinition> pools
) {

    public static final Codec<LootTableDefinition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("identify").forGetter(LootTableDefinition::identify),
                    Codec.STRING.optionalFieldOf("note").forGetter(LootTableDefinition::note),
                    ResourceLocation.CODEC.fieldOf("loot_type").forGetter(LootTableDefinition::lootType),
                    LootPoolDefinition.CODEC.listOf().fieldOf("pools").forGetter(LootTableDefinition::pools)
            ).apply(instance, LootTableDefinition::new)
    );

    public WeightedRandomList<WeightedEntry.Wrapper<LootPoolDefinition>> poolsAsWeightedList() {
        return WeightedRandomList.create(pools.stream()
                .filter(pool -> pool.baseWeight() > 0)
                .map(LootPoolDefinition::toWeighted)
                .toList());
    }
}
