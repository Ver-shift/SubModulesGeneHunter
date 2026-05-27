package org.galaxy.beyond.api.datagen.custom;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.galaxy.beyond.Beyond;

import java.util.concurrent.CompletableFuture;

public class BeyondStructureTagsProvider extends StructureTagsProvider {
    public BeyondStructureTagsProvider(PackOutput output,
                                       CompletableFuture<HolderLookup.Provider> lookupProvider,
                                       ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Beyond.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BeyondWorldgenKeys.SAFE_ZONE_STRUCTURE)
                .add(BeyondWorldgenKeys.SAFE_STRUCTURE);
        tag(BeyondWorldgenKeys.NODE_STRUCTURE)
                .add(BeyondWorldgenKeys.PILLAGER_CAMP);
    }
}
