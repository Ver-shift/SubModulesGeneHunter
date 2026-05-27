package org.galaxy.beyond.api.datagen.custom;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.galaxy.beyond.Beyond;

import java.util.concurrent.CompletableFuture;

public class BeyondBiomeTagsProvider extends BiomeTagsProvider {
    public BeyondBiomeTagsProvider(PackOutput output,
                                   CompletableFuture<HolderLookup.Provider> lookupProvider,
                                   ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Beyond.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BeyondWorldgenKeys.HAS_PILLAGER_CAMP)
                .add(Biomes.PLAINS)
                .add(Biomes.SUNFLOWER_PLAINS)
                .add(Biomes.MEADOW)
                .add(Biomes.SNOWY_PLAINS);
    }
}
